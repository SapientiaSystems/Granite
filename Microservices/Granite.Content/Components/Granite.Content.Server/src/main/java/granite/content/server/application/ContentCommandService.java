//////////////////////////////////////////////////////////////////////////////
// ContentCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.application;

import granite.content.server.domain.ContentDocument;
import granite.content.server.domain.IContentDocumentEvent;
import granite.content.server.domain.IContentDocumentEventObserver;
import granite.content.server.domain.IContentDocumentRepository;
import granite.content.service.event.ContentEvent;
import granite.content.service.event.IContentEventSender;
import granite.content.service.requestreply.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.event.EventIdentifiersData;
import strata.foundation.core.event.StandardEventType;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public
class ContentCommandService
    implements ITransactionalContentCommandService,IContentDocumentEventObserver
{
    public static final Pattern KEY_PATTERN     = Pattern.compile("[a-z0-9][a-z0-9._/-]{0,254}");
    public static final int     MAX_TITLE_LENGTH = 512;
    public static final int     MAX_BODY_LENGTH  = 200000;

    private final IContentDocumentRepository repository;
    private final IContentEventSender        sender;
    private final ModelMapper                mapper;
    private final Clock                      clock;
    private final Logger                     logger;

    public
    ContentCommandService(
        IContentDocumentRepository r,
        IContentEventSender        s,
        ModelMapper                m,
        Clock                      c)
    {
        repository = r;
        sender     = s;
        mapper     = m;
        clock      = c;
        logger     = LogManager.getLogger(ContentCommandService.class);
    }

    @Override
    public CompletionStage<UpsertContentDocumentsReply>
    upsertContentDocuments(UpsertContentDocumentsRequest request)
    {
        logger.debug("upsertContentDocuments({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateUpsert(request),new CurrentThreadExecutor())
                .thenApply(this::upsert)
                .thenApply(upserted -> buildUpsertReply(request,upserted))
                .exceptionally(e -> buildUpsertFailure(request,e));
    }

    @Override
    public CompletionStage<DestroyContentDocumentsReply>
    destroyContentDocuments(DestroyContentDocumentsRequest request)
    {
        logger.debug("destroyContentDocuments({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateKeys(request),new CurrentThreadExecutor())
                .thenApply(this::destroy)
                .thenApply(destroyed -> buildDestroyReply(request,destroyed))
                .exceptionally(e -> buildDestroyFailure(request,e));
    }

    @Override
    public CompletionStage<UpsertContentDocumentsReply>
    synchronizeSource(String source,List<UpsertContentDocumentData> documents)
    {
        Objects.requireNonNull(source,"source");

        List<UpsertContentDocumentData> stamped = new ArrayList<>();

        for (UpsertContentDocumentData document : documents == null ? List.<UpsertContentDocumentData>of() : documents)
            stamped.add(document.setSource(source));

        UpsertContentDocumentsRequest request = new UpsertContentDocumentsRequest().setDocuments(stamped);

        return
            CompletableFuture
                .supplyAsync(() -> stamped.isEmpty() ? stamped : validateUpsert(request),new CurrentThreadExecutor())
                .thenApply(valid -> prune(source,valid,upsert(valid)))
                .thenApply(upserted -> buildUpsertReply(request,upserted))
                .exceptionally(e -> buildUpsertFailure(request,e));
    }

    @Override
    public void
    onEvent(IContentDocumentEvent event)
    {
        switch (event.getName())
        {
            case "ContentDocumentCreated":
                sender.send(toServiceEvent(event,StandardEventType.CREATED));
                break;

            case "ContentDocumentUpdated":
                sender.send(toServiceEvent(event,StandardEventType.UPDATED));
                break;

            case "ContentDocumentDestroyed":
                sender.send(toServiceEvent(event,StandardEventType.DESTROYED));
                break;

            default:
                logger.error("Encountered unknown event type: {}",event.getName());
                break;
        }
    }

    @Override
    public void
    onException(Exception e)
    {
        logger.error("Observing ContentEvents and caught exception: {}",e.getMessage());
    }

    protected List<UpsertContentDocumentData>
    validateUpsert(UpsertContentDocumentsRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getDocuments(),"validation failed: documents were not provided");

        if (request.getDocuments().isEmpty())
            throw new IllegalArgumentException("validation failed: documents were empty");

        Set<String> keys = new TreeSet<>();

        for (UpsertContentDocumentData data : request.getDocuments())
        {
            Objects.requireNonNull(data,"validation failed: document was null");

            if (data.getKey() == null || !KEY_PATTERN.matcher(data.getKey()).matches())
                throw new IllegalArgumentException("validation failed: invalid key " + data.getKey());

            if (!keys.add(data.getKey()))
                throw new IllegalArgumentException("validation failed: duplicate key " + data.getKey());

            if (data.getTitle() == null || data.getTitle().isBlank() || data.getTitle().length() > MAX_TITLE_LENGTH)
                throw new IllegalArgumentException("validation failed: title of " + data.getKey() + " is blank or too long");

            if (data.getBody() != null && data.getBody().length() > MAX_BODY_LENGTH)
                throw new IllegalArgumentException("validation failed: body of " + data.getKey() + " is longer than " + MAX_BODY_LENGTH + " characters");
        }

        return request.getDocuments();
    }

    protected Set<String>
    validateKeys(DestroyContentDocumentsRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getKeys(),"validation failed: keys were not provided");

        if (request.getKeys().isEmpty())
            throw new IllegalArgumentException("validation failed: keys were empty");

        return request.getKeys();
    }

    protected List<ContentDocument>
    upsert(List<UpsertContentDocumentData> documents)
    {
        Instant                               now      = Instant.now(clock);
        Set<String>                           keys     = documents.stream().map(UpsertContentDocumentData::getKey).collect(Collectors.toCollection(TreeSet::new));
        Map<String,Optional<ContentDocument>> existing = repository.findAllByKeyIn(keys);
        List<ContentDocument>                 result   = new ArrayList<>();

        for (UpsertContentDocumentData data : documents)
        {
            Optional<ContentDocument> found = existing.getOrDefault(data.getKey(),Optional.empty());

            if (found.isPresent() && unchanged(found.get(),data))
            {
                result.add(found.get());
                continue;
            }

            ContentDocument document = apply(found.orElseGet(ContentDocument::new),data).setChanged(now);
            ContentDocument saved    = repository.save(document).attach(this);

            result.add(found.isPresent() ? saved.notifyUpdated() : saved.notifyCreated());
        }

        return result;
    }

    protected Set<String>
    destroy(Set<String> keys)
    {
        Set<String> destroyed = new TreeSet<>();

        for (Map.Entry<String,Optional<ContentDocument>> entry : repository.findAllByKeyIn(keys).entrySet())
        {
            if (entry.getValue().isEmpty())
                continue;

            entry.getValue().get().attach(this).notifyDestroyed();
            repository.delete(entry.getValue().get());
            destroyed.add(entry.getKey());
        }

        return destroyed;
    }

    protected List<ContentDocument>
    prune(String source,List<UpsertContentDocumentData> kept,List<ContentDocument> upserted)
    {
        Set<String> keys  = kept.stream().map(UpsertContentDocumentData::getKey).collect(Collectors.toCollection(TreeSet::new));
        Set<String> stale = new TreeSet<>();

        for (ContentDocument document : repository.findAllBySource(source))
            if (!keys.contains(document.getDocumentKey()))
                stale.add(document.getDocumentKey());

        if (!stale.isEmpty())
            logger.info("<synchronizeSource> {} removed {} document(s)",source,destroy(stale).size());

        return upserted;
    }

    protected boolean
    unchanged(ContentDocument document,UpsertContentDocumentData data)
    {
        return
            data.getVersion() != null &&
            data.getVersion().equals(document.getContentVersion()) &&
            Objects.equals(data.getSource(),document.getSource());
    }

    protected ContentDocument
    apply(ContentDocument document,UpsertContentDocumentData data)
    {
        return
            document
                .setDocumentKey(data.getKey())
                .setTitle(data.getTitle().trim())
                .setKind(data.getKind() == null ? ContentKind.HELP : data.getKind())
                .setFormat(data.getFormat() == null ? ContentFormat.MARKDOWN : data.getFormat())
                .setBody(data.getBody() == null ? "" : data.getBody())
                .setKeywords(data.getKeywords())
                .setRequiredAuthorities(data.getRequiredAuthorities())
                .setSource(data.getSource())
                .setContentVersion(data.getVersion());
    }

    private UpsertContentDocumentsReply
    buildUpsertReply(UpsertContentDocumentsRequest request,List<ContentDocument> upserted)
    {
        Set<String> requested = requestedKeys(request);
        Set<String> done      = upserted.stream().map(ContentDocument::getDocumentKey).collect(Collectors.toCollection(TreeSet::new));
        Set<String> failed    = requested.stream().filter(k -> !done.contains(k)).collect(Collectors.toCollection(TreeSet::new));

        return
            new UpsertContentDocumentsReply(request)
                .setSuccess(failed.isEmpty())
                .setSuccessMessage(failed.isEmpty() ? "Upserted " + done.size() + " content document(s)" : null)
                .setFailureMessage(failed.isEmpty() ? null : "Unable to upsert " + failed.size() + " content document(s)")
                .setRequestedKeys(requested)
                .setFailedKeys(failed)
                .setUpsertedDocuments(upserted.stream().map(d -> mapper.map(d,ContentDocumentData.class)).collect(Collectors.toList()));
    }

    private UpsertContentDocumentsReply
    buildUpsertFailure(UpsertContentDocumentsRequest request,Throwable e)
    {
        Set<String> requested = requestedKeys(request);

        logger.info("<upsertContentDocuments> exception reply: {}",e.getMessage());
        return
            new UpsertContentDocumentsReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to upsert content documents: " + rootMessage(e))
                .setException(ExceptionData.of(e))
                .setRequestedKeys(requested)
                .setFailedKeys(new TreeSet<>(requested))
                .setUpsertedDocuments(List.of());
    }

    private DestroyContentDocumentsReply
    buildDestroyReply(DestroyContentDocumentsRequest request,Set<String> destroyed)
    {
        Set<String> requested = new TreeSet<>(request.getKeys());
        Set<String> failed    = requested.stream().filter(k -> !destroyed.contains(k)).collect(Collectors.toCollection(TreeSet::new));

        return
            new DestroyContentDocumentsReply(request)
                .setSuccess(failed.isEmpty())
                .setSuccessMessage(failed.isEmpty() ? "Destroyed " + destroyed.size() + " content document(s)" : null)
                .setFailureMessage(failed.isEmpty() ? null : "Unable to destroy " + failed.size() + " content document(s)")
                .setRequestedKeys(requested)
                .setFailedKeys(failed)
                .setDestroyedKeys(destroyed);
    }

    private DestroyContentDocumentsReply
    buildDestroyFailure(DestroyContentDocumentsRequest request,Throwable e)
    {
        Set<String> requested = request == null || request.getKeys() == null ? new TreeSet<>() : new TreeSet<>(request.getKeys());

        logger.info("<destroyContentDocuments> exception reply: {}",e.getMessage());
        return
            new DestroyContentDocumentsReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to destroy content documents: " + rootMessage(e))
                .setException(ExceptionData.of(e))
                .setRequestedKeys(requested)
                .setFailedKeys(new TreeSet<>(requested))
                .setDestroyedKeys(new TreeSet<>());
    }

    private Set<String>
    requestedKeys(UpsertContentDocumentsRequest request)
    {
        if (request == null || request.getDocuments() == null)
            return new TreeSet<>();

        return
            request
                .getDocuments()
                .stream()
                .filter(Objects::nonNull)
                .map(UpsertContentDocumentData::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private String
    rootMessage(Throwable e)
    {
        Throwable cause = e;

        while (cause.getCause() != null && cause != cause.getCause())
            cause = cause.getCause();

        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }

    private ContentEvent
    toServiceEvent(IContentDocumentEvent domainEvent,StandardEventType eventType)
    {
        return
            ContentEvent
                .newBuilder()
                .setEventType(eventType)
                .setIdentifiers(
                    EventIdentifiersData
                        .newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setCorrelationId(domainEvent.getCorrelationId())
                        .setTimestamp(domainEvent.getTimestamp())
                        .build())
                .setSourceType(domainEvent.getSource().getClass().getCanonicalName())
                .setSourceId(domainEvent.getSource().getPrimaryId())
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
