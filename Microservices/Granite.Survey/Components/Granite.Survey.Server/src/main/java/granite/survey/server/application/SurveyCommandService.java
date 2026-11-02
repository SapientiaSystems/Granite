//////////////////////////////////////////////////////////////////////////////
// SurveyCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.application;

import granite.survey.server.domain.ISurveyResponseEvent;
import granite.survey.server.domain.ISurveyResponseEventObserver;
import granite.survey.server.domain.ISurveyResponseRepository;
import granite.survey.server.domain.SurveyResponse;
import granite.survey.service.event.ISurveyEventSender;
import granite.survey.service.event.SurveyEvent;
import granite.survey.service.requestreply.*;
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
import java.util.LinkedHashMap;
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
class SurveyCommandService
    implements ITransactionalSurveyCommandService,ISurveyResponseEventObserver
{
    private final ISurveyResponseRepository repository;
    private final ISurveyEventSender        sender;
    private final ModelMapper               mapper;
    private final Clock                     clock;
    private final Logger                    logger;

    public
    SurveyCommandService(
        ISurveyResponseRepository r,
        ISurveyEventSender        s,
        ModelMapper               m,
        Clock                     c)
    {
        repository = r;
        sender     = s;
        mapper     = m;
        clock      = c;
        logger     = LogManager.getLogger(SurveyCommandService.class);
    }

    @Override
    public CompletionStage<SubmitSurveyResponseReply>
    submitSurveyResponse(SubmitSurveyResponseRequest request)
    {
        logger.debug("submitSurveyResponse({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateSubmit(request),new CurrentThreadExecutor())
                .thenApply(this::submit)
                .thenApply(submitted -> buildSubmitReply(request,submitted))
                .exceptionally(e -> buildSubmitReply(e,request));
    }

    @Override
    public CompletionStage<UpdateSurveyResponsesReply>
    updateSurveyResponses(UpdateSurveyResponsesRequest request)
    {
        logger.debug("updateSurveyResponses({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateUpdate(request),new CurrentThreadExecutor())
                .thenApply(this::update)
                .thenApply(updated -> buildPluralReply(new UpdateSurveyResponsesReply(request),requestedIds(request),updated))
                .exceptionally(e -> buildPluralFailure(new UpdateSurveyResponsesReply(request),requestedIds(request),"Unable to update survey responses",e));
    }

    @Override
    public CompletionStage<MarkSurveyResponsesSentReply>
    markSurveyResponsesSent(MarkSurveyResponsesSentRequest request)
    {
        logger.debug("markSurveyResponsesSent({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateIds(request == null ? null : request.getResponseIds()),new CurrentThreadExecutor())
                .thenApply(ids -> mark(ids,SurveyResponse::canMarkSent,SurveyResponseStatus.SENT))
                .thenApply(marked -> buildPluralReply(new MarkSurveyResponsesSentReply(request),request.getResponseIds(),marked))
                .exceptionally(e -> buildPluralFailure(new MarkSurveyResponsesSentReply(request),request == null ? null : request.getResponseIds(),"Unable to mark survey responses sent",e));
    }

    @Override
    public CompletionStage<MarkSurveyResponsesReceivedReply>
    markSurveyResponsesReceived(MarkSurveyResponsesReceivedRequest request)
    {
        logger.debug("markSurveyResponsesReceived({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateIds(request == null ? null : request.getResponseIds()),new CurrentThreadExecutor())
                .thenApply(ids -> mark(ids,SurveyResponse::canMarkReceived,SurveyResponseStatus.RECEIVED))
                .thenApply(marked -> buildPluralReply(new MarkSurveyResponsesReceivedReply(request),request.getResponseIds(),marked))
                .exceptionally(e -> buildPluralFailure(new MarkSurveyResponsesReceivedReply(request),request == null ? null : request.getResponseIds(),"Unable to mark survey responses received",e));
    }

    @Override
    public void
    onEvent(ISurveyResponseEvent event)
    {
        switch (event.getName())
        {
            case "SurveyResponseCreated":
                sender.send(toServiceEvent(event,StandardEventType.CREATED));
                break;

            case "SurveyResponseUpdated":
                sender.send(toServiceEvent(event,StandardEventType.UPDATED));
                break;

            case "SurveyResponseDestroyed":
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
        logger.error("Observing SurveyEvents and caught exception: {}",e.getMessage());
    }

    protected SubmitSurveyResponseData
    validateSubmit(SubmitSurveyResponseRequest request)
    {
        Objects.requireNonNull(request,"request");

        SubmitSurveyResponseData data = request.getResponse();

        Objects.requireNonNull(data,"validation failed: response was not provided");
        Objects.requireNonNull(data.getUserId(),"validation failed: userId was not provided");
        Objects.requireNonNull(data.getAnswers(),"validation failed: answers were not provided");

        Set<String> expected = new TreeSet<>(GraniteSurveyQuestions.keys());

        for (Map.Entry<String,Integer> answer : data.getAnswers().entrySet())
        {
            if (!expected.contains(answer.getKey()))
                throw new IllegalArgumentException("validation failed: unknown question " + answer.getKey());

            if (answer.getValue() == null
                || answer.getValue() < GraniteSurveyQuestions.MIN_ANSWER
                || answer.getValue() > GraniteSurveyQuestions.MAX_ANSWER)
                throw new IllegalArgumentException(
                    "validation failed: answer to " + answer.getKey() + " must be between "
                        + GraniteSurveyQuestions.MIN_ANSWER + " and " + GraniteSurveyQuestions.MAX_ANSWER);
        }

        for (String key : expected)
            if (!data.getAnswers().containsKey(key))
                throw new IllegalArgumentException("validation failed: question " + key + " was not answered");

        if (data.getComment() != null && data.getComment().length() > GraniteSurveyQuestions.MAX_COMMENT_LENGTH)
            throw new IllegalArgumentException(
                "validation failed: comment is longer than " + GraniteSurveyQuestions.MAX_COMMENT_LENGTH + " characters");

        return data;
    }

    protected List<UpdateSurveyResponseData>
    validateUpdate(UpdateSurveyResponsesRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getResponses(),"validation failed: responses were not provided");

        if (request.getResponses().isEmpty())
            throw new IllegalArgumentException("validation failed: responses were empty");

        for (UpdateSurveyResponseData data : request.getResponses())
            Objects.requireNonNull(data.getResponseId(),"validation failed: responseId was not provided");

        return request.getResponses();
    }

    protected Set<Long>
    validateIds(Set<Long> ids)
    {
        Objects.requireNonNull(ids,"validation failed: responseIds were not provided");

        if (ids.isEmpty())
            throw new IllegalArgumentException("validation failed: responseIds were empty");

        return ids;
    }

    protected SurveyResponse
    submit(SubmitSurveyResponseData data)
    {
        Map<String,Integer> answers = new LinkedHashMap<>();
        String              comment = data.getComment() == null || data.getComment().isBlank() ? null : data.getComment().trim();

        for (String key : GraniteSurveyQuestions.keys())
            answers.put(key,data.getAnswers().get(key));

        SurveyResponse response =
            new SurveyResponse()
                .setUserId(data.getUserId())
                .setAnswers(answers)
                .setComment(comment)
                .setSubmitted(Instant.now(clock))
                .setChanged(Instant.now(clock));

        return repository.save(response).attach(this).notifyCreated();
    }

    protected List<SurveyResponse>
    update(List<UpdateSurveyResponseData> changes)
    {
        Instant                            now     = Instant.now(clock);
        Set<Long>                          ids     = changes.stream().map(UpdateSurveyResponseData::getResponseId).collect(Collectors.toCollection(TreeSet::new));
        Map<Long,Optional<SurveyResponse>> loaded  = repository.findAllByIdIn(ids);
        List<SurveyResponse>               updated = new ArrayList<>();

        for (UpdateSurveyResponseData change : changes)
        {
            Optional<SurveyResponse> found = loaded.getOrDefault(change.getResponseId(),Optional.empty());

            if (found.isEmpty())
                continue;

            SurveyResponse response =
                found
                    .get()
                    .changeStatus(change.getStatus(),now)
                    .setNotes(change.getNotes())
                    .setChanged(now);
            updated.add(repository.save(response).attach(this).notifyUpdated());
        }

        return updated;
    }

    protected List<SurveyResponse>
    mark(Set<Long> ids,Predicate<SurveyResponse> allowed,SurveyResponseStatus status)
    {
        Instant              now    = Instant.now(clock);
        List<SurveyResponse> marked = new ArrayList<>();

        for (Optional<SurveyResponse> found : repository.findAllByIdIn(ids).values())
        {
            if (found.isEmpty() || !allowed.test(found.get()))
                continue;

            SurveyResponse response = found.get().changeStatus(status,now).setChanged(now);

            marked.add(repository.save(response).attach(this).notifyUpdated());
        }

        return marked;
    }

    private SubmitSurveyResponseReply
    buildSubmitReply(SubmitSurveyResponseRequest request,SurveyResponse submitted)
    {
        logger.info("<submitSurveyResponse> created survey response {}",submitted.getPrimaryId());
        return
            new SubmitSurveyResponseReply(request)
                .setSuccess(true)
                .setSuccessMessage("Survey response: " + submitted.getPrimaryId() + " created")
                .setSubmittedResponse(mapper.map(submitted,SurveyResponseData.class));
    }

    private SubmitSurveyResponseReply
    buildSubmitReply(Throwable e,SubmitSurveyResponseRequest request)
    {
        logger.info("<submitSurveyResponse> exception reply: {}",e.getMessage());
        return
            new SubmitSurveyResponseReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to submit survey response: " + rootMessage(e))
                .setException(ExceptionData.of(e));
    }

    private <R extends AbstractServiceReply> R
    buildPluralReply(R reply,Set<Long> requestedIds,List<SurveyResponse> succeeded)
    {
        Set<Long>                requested = requestedIds == null ? new TreeSet<>() : new TreeSet<>(requestedIds);
        Set<Long>                done      = succeeded.stream().map(SurveyResponse::getPrimaryId).collect(Collectors.toCollection(TreeSet::new));
        Set<Long>                failed    = requested.stream().filter(id -> !done.contains(id)).collect(Collectors.toCollection(TreeSet::new));
        List<SurveyResponseData> data      = succeeded.stream().map(r -> mapper.map(r,SurveyResponseData.class)).collect(Collectors.toList());

        reply
            .setSuccess(failed.isEmpty())
            .setSuccessMessage(failed.isEmpty() ? "Changed " + done.size() + " survey response(s)" : null)
            .setFailureMessage(failed.isEmpty() ? null : "Unable to change " + failed.size() + " survey response(s)");
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
    setPlural(R reply,Set<Long> requested,Set<Long> failed,List<SurveyResponseData> data)
    {
        if (reply instanceof UpdateSurveyResponsesReply r)
            return (R)r.setRequestedResponseIds(requested).setFailedResponseIds(failed).setUpdatedResponses(data);

        if (reply instanceof MarkSurveyResponsesSentReply r)
            return (R)r.setRequestedResponseIds(requested).setFailedResponseIds(failed).setUpdatedResponses(data);

        if (reply instanceof MarkSurveyResponsesReceivedReply r)
            return (R)r.setRequestedResponseIds(requested).setFailedResponseIds(failed).setUpdatedResponses(data);

        return reply;
    }

    private Set<Long>
    requestedIds(UpdateSurveyResponsesRequest request)
    {
        if (request == null || request.getResponses() == null)
            return new TreeSet<>();

        return
            request
                .getResponses()
                .stream()
                .map(UpdateSurveyResponseData::getResponseId)
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

    private SurveyEvent
    toServiceEvent(ISurveyResponseEvent domainEvent,StandardEventType eventType)
    {
        return
            SurveyEvent
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
