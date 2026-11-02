//////////////////////////////////////////////////////////////////////////////
// FeedbackQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.application;

import granite.feedback.server.domain.Feedback;
import granite.feedback.server.domain.IFeedbackRepository;
import granite.feedback.service.requestreply.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.transfer.ExceptionData;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public
class FeedbackQueryService
    implements ITransactionalFeedbackQueryService
{
    private final IFeedbackRepository repository;
    private final ModelMapper         mapper;
    private final Logger              logger;

    public
    FeedbackQueryService(IFeedbackRepository r,ModelMapper m)
    {
        repository = r;
        mapper     = m;
        logger     = LogManager.getLogger(FeedbackQueryService.class);
    }

    @Override
    public CompletionStage<FindFeedbackByIdsReply>
    findFeedbackByIds(FindFeedbackByIdsRequest request)
    {
        logger.debug("findFeedbackByIds({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(
                    valid ->
                        repository
                            .findAllByIdIn(valid.getFeedbackIds())
                            .values()
                            .stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()))
                .thenApply(
                    found ->
                        new FindFeedbackByIdsReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Found " + found.size() + " feedback record(s)")
                            .setFeedback(toData(found)))
                .exceptionally(
                    e ->
                        new FindFeedbackByIdsReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to find feedback: " + e.getMessage())
                            .setException(ExceptionData.of(e)));
    }

    @Override
    public CompletionStage<FindAllFeedbackReply>
    findAllFeedback(FindAllFeedbackRequest request)
    {
        logger.debug("findAllFeedback({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> Objects.requireNonNull(request,"request"),new CurrentThreadExecutor())
                .thenApply(
                    valid ->
                        valid.getStatuses() == null || valid.getStatuses().isEmpty()
                            ? repository.findAllFeedback()
                            : repository.findAllByStatusIn(valid.getStatuses()))
                .thenApply(
                    found ->
                        new FindAllFeedbackReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Found " + found.size() + " feedback record(s)")
                            .setFeedback(toData(found)))
                .exceptionally(
                    e ->
                        new FindAllFeedbackReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to find feedback: " + e.getMessage())
                            .setException(ExceptionData.of(e)));
    }

    protected FindFeedbackByIdsRequest
    validate(FindFeedbackByIdsRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getFeedbackIds(),"validation failed: feedbackIds were not provided");

        if (request.getFeedbackIds().isEmpty())
            throw new IllegalArgumentException("validation failed: feedbackIds were empty");

        return request;
    }

    private List<FeedbackData>
    toData(List<Feedback> found)
    {
        return found.stream().map(f -> mapper.map(f,FeedbackData.class)).collect(Collectors.toList());
    }
}

//////////////////////////////////////////////////////////////////////////////
