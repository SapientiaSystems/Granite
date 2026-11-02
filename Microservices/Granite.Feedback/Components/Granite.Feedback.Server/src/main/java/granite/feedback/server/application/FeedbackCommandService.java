//////////////////////////////////////////////////////////////////////////////
// FeedbackCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.application;

import granite.feedback.server.domain.Feedback;
import granite.feedback.server.domain.IFeedbackEvent;
import granite.feedback.server.domain.IFeedbackEventObserver;
import granite.feedback.server.domain.IFeedbackRepository;
import granite.feedback.service.event.FeedbackEvent;
import granite.feedback.service.event.IFeedbackEventSender;
import granite.feedback.service.requestreply.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.event.EventIdentifiersData;
import strata.foundation.core.event.StandardEventType;
import strata.foundation.core.transfer.AbstractServiceReply;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

public
class FeedbackCommandService
    implements ITransactionalFeedbackCommandService,IFeedbackEventObserver
{
    public static final int MAX_SUMMARY_LENGTH = 512;

    private final IFeedbackRepository  repository;
    private final IFeedbackEventSender sender;
    private final ModelMapper          mapper;
    private final Clock                clock;
    private final Logger               logger;

    public
    FeedbackCommandService(
        IFeedbackRepository  r,
        IFeedbackEventSender s,
        ModelMapper          m,
        Clock                c)
    {
        repository = r;
        sender     = s;
        mapper     = m;
        clock      = c;
        logger     = LogManager.getLogger(FeedbackCommandService.class);
    }

    @Override
    public CompletionStage<CreateFeedbackReply>
    createFeedback(CreateFeedbackRequest request)
    {
        logger.debug("createFeedback({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateCreate(request),new CurrentThreadExecutor())
                .thenApply(this::create)
                .thenApply(created -> buildCreateReply(request,created))
                .exceptionally(e -> buildCreateReply(e,request));
    }

    @Override
    public CompletionStage<UpdateFeedbacksReply>
    updateFeedbacks(UpdateFeedbacksRequest request)
    {
        logger.debug("updateFeedbacks({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateUpdate(request),new CurrentThreadExecutor())
                .thenApply(this::update)
                .thenApply(updated -> buildPluralReply(new UpdateFeedbacksReply(request),requestedIds(request),updated))
                .exceptionally(e -> buildPluralFailure(new UpdateFeedbacksReply(request),requestedIds(request),"Unable to update feedback",e));
    }

    @Override
    public CompletionStage<MarkFeedbackSentReply>
    markFeedbackSent(MarkFeedbackSentRequest request)
    {
        logger.debug("markFeedbackSent({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateIds(request == null ? null : request.getFeedbackIds()),new CurrentThreadExecutor())
                .thenApply(ids -> mark(ids,Feedback::canMarkSent,FeedbackStatus.SENT))
                .thenApply(marked -> buildPluralReply(new MarkFeedbackSentReply(request),request.getFeedbackIds(),marked))
                .exceptionally(e -> buildPluralFailure(new MarkFeedbackSentReply(request),request == null ? null : request.getFeedbackIds(),"Unable to mark feedback sent",e));
    }

    @Override
    public CompletionStage<MarkFeedbackReceivedReply>
    markFeedbackReceived(MarkFeedbackReceivedRequest request)
    {
        logger.debug("markFeedbackReceived({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateIds(request == null ? null : request.getFeedbackIds()),new CurrentThreadExecutor())
                .thenApply(ids -> mark(ids,Feedback::canMarkReceived,FeedbackStatus.RECEIVED))
                .thenApply(marked -> buildPluralReply(new MarkFeedbackReceivedReply(request),request.getFeedbackIds(),marked))
                .exceptionally(e -> buildPluralFailure(new MarkFeedbackReceivedReply(request),request == null ? null : request.getFeedbackIds(),"Unable to mark feedback received",e));
    }

    @Override
    public void
    onEvent(IFeedbackEvent event)
    {
        switch (event.getName())
        {
            case "FeedbackCreated":
                sender.send(toServiceEvent(event,StandardEventType.CREATED));
                break;

            case "FeedbackUpdated":
                sender.send(toServiceEvent(event,StandardEventType.UPDATED));
                break;

            case "FeedbackDestroyed":
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
        logger.error("Observing FeedbackEvents and caught exception: {}",e.getMessage());
    }

    protected CreateFeedbackData
    validateCreate(CreateFeedbackRequest request)
    {
        Objects.requireNonNull(request,"request");

        CreateFeedbackData data = request.getFeedback();

        Objects.requireNonNull(data,"validation failed: feedback was not provided");
        Objects.requireNonNull(data.getUserId(),"validation failed: userId was not provided");
        Objects.requireNonNull(data.getType(),"validation failed: type was not provided");

        if (data.getSummary() == null || data.getSummary().isBlank())
            throw new IllegalArgumentException("validation failed: summary was blank");

        if (data.getSummary().length() > MAX_SUMMARY_LENGTH)
            throw new IllegalArgumentException("validation failed: summary is longer than " + MAX_SUMMARY_LENGTH + " characters");

        return data;
    }

    protected List<UpdateFeedbackData>
    validateUpdate(UpdateFeedbacksRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getFeedback(),"validation failed: feedback was not provided");

        if (request.getFeedback().isEmpty())
            throw new IllegalArgumentException("validation failed: feedback was empty");

        for (UpdateFeedbackData data : request.getFeedback())
            Objects.requireNonNull(data.getFeedbackId(),"validation failed: feedbackId was not provided");

        return request.getFeedback();
    }

    protected Set<Long>
    validateIds(Set<Long> ids)
    {
        Objects.requireNonNull(ids,"validation failed: feedbackIds were not provided");

        if (ids.isEmpty())
            throw new IllegalArgumentException("validation failed: feedbackIds were empty");

        return ids;
    }

    protected Feedback
    create(CreateFeedbackData data)
    {
        Feedback feedback =
            new Feedback()
                .setUserId(data.getUserId())
                .setType(data.getType())
                .setSummary(data.getSummary().trim())
                .setDetails(data.getDetails())
                .setSubmitted(Instant.now(clock))
                .setChanged(Instant.now(clock));

        return repository.save(feedback).attach(this).notifyCreated();
    }

    protected List<Feedback>
    update(List<UpdateFeedbackData> changes)
    {
        Instant                      now     = Instant.now(clock);
        Set<Long>                    ids     = changes.stream().map(UpdateFeedbackData::getFeedbackId).collect(Collectors.toCollection(TreeSet::new));
        Map<Long,Optional<Feedback>> loaded  = repository.findAllByIdIn(ids);
        List<Feedback>               updated = new ArrayList<>();

        for (UpdateFeedbackData change : changes)
        {
            Optional<Feedback> found = loaded.getOrDefault(change.getFeedbackId(),Optional.empty());

            if (found.isEmpty())
                continue;

            Feedback feedback =
                found
                    .get()
                    .changeStatus(change.getStatus(),now)
                    .setNotes(change.getNotes())
                    .setChanged(now);
            updated.add(repository.save(feedback).attach(this).notifyUpdated());
        }

        return updated;
    }

    protected List<Feedback>
    mark(Set<Long> ids,Predicate<Feedback> allowed,FeedbackStatus status)
    {
        Instant        now    = Instant.now(clock);
        List<Feedback> marked = new ArrayList<>();

        for (Optional<Feedback> found : repository.findAllByIdIn(ids).values())
        {
            if (found.isEmpty() || !allowed.test(found.get()))
                continue;

            Feedback feedback = found.get().changeStatus(status,now).setChanged(now);

            marked.add(repository.save(feedback).attach(this).notifyUpdated());
        }

        return marked;
    }

    private CreateFeedbackReply
    buildCreateReply(CreateFeedbackRequest request,Feedback created)
    {
        logger.info("<createFeedback> created feedback {}",created.getPrimaryId());
        return
            new CreateFeedbackReply(request)
                .setSuccess(true)
                .setSuccessMessage("Feedback: " + created.getPrimaryId() + " created")
                .setCreatedFeedback(mapper.map(created,FeedbackData.class));
    }

    private CreateFeedbackReply
    buildCreateReply(Throwable e,CreateFeedbackRequest request)
    {
        logger.info("<createFeedback> exception reply: {}",e.getMessage());
        return
            new CreateFeedbackReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to create feedback: " + rootMessage(e))
                .setException(ExceptionData.of(e));
    }

    private <R extends AbstractServiceReply> R
    buildPluralReply(R reply,Set<Long> requestedIds,List<Feedback> succeeded)
    {
        Set<Long>          requested = requestedIds == null ? new TreeSet<>() : new TreeSet<>(requestedIds);
        Set<Long>          done      = succeeded.stream().map(Feedback::getPrimaryId).collect(Collectors.toCollection(TreeSet::new));
        Set<Long>          failed    = requested.stream().filter(id -> !done.contains(id)).collect(Collectors.toCollection(TreeSet::new));
        List<FeedbackData> data      = succeeded.stream().map(f -> mapper.map(f,FeedbackData.class)).collect(Collectors.toList());

        reply
            .setSuccess(failed.isEmpty())
            .setSuccessMessage(failed.isEmpty() ? "Changed " + done.size() + " feedback record(s)" : null)
            .setFailureMessage(failed.isEmpty() ? null : "Unable to change " + failed.size() + " feedback record(s)");
        return setPlural(reply,requested,failed,data);
    }

    private <R extends AbstractServiceReply> R
    buildPluralFailure(R reply,Set<Long> requestedIds,String message,Throwable e)
    {
        Set<Long> requested = requestedIds == null ? new TreeSet<>() : new TreeSet<>(requestedIds);

        logger.info("{}: {}",message,e.getMessage());
        reply
            .setSuccess(false)
            .setFailureMessage(message + ": " + rootMessage(e))
            .setException(ExceptionData.of(e));
        return setPlural(reply,requested,new TreeSet<>(requested),List.of());
    }

    @SuppressWarnings("unchecked")
    private <R> R
    setPlural(R reply,Set<Long> requested,Set<Long> failed,List<FeedbackData> data)
    {
        if (reply instanceof UpdateFeedbacksReply r)
            return (R)r.setRequestedFeedbackIds(requested).setFailedFeedbackIds(failed).setUpdatedFeedback(data);

        if (reply instanceof MarkFeedbackSentReply r)
            return (R)r.setRequestedFeedbackIds(requested).setFailedFeedbackIds(failed).setUpdatedFeedback(data);

        if (reply instanceof MarkFeedbackReceivedReply r)
            return (R)r.setRequestedFeedbackIds(requested).setFailedFeedbackIds(failed).setUpdatedFeedback(data);

        return reply;
    }

    private Set<Long>
    requestedIds(UpdateFeedbacksRequest request)
    {
        if (request == null || request.getFeedback() == null)
            return new TreeSet<>();

        return
            request
                .getFeedback()
                .stream()
                .map(UpdateFeedbackData::getFeedbackId)
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

    private FeedbackEvent
    toServiceEvent(IFeedbackEvent domainEvent,StandardEventType eventType)
    {
        return
            FeedbackEvent
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
