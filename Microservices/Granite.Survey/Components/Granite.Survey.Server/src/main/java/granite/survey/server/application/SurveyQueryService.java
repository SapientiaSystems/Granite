//////////////////////////////////////////////////////////////////////////////
// SurveyQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.application;

import granite.survey.server.domain.ISurveyResponseRepository;
import granite.survey.server.domain.SurveyResponse;
import granite.survey.service.requestreply.*;
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
class SurveyQueryService
    implements ITransactionalSurveyQueryService
{
    private final ISurveyResponseRepository repository;
    private final ModelMapper               mapper;
    private final Logger                    logger;

    public
    SurveyQueryService(ISurveyResponseRepository r,ModelMapper m)
    {
        repository = r;
        mapper     = m;
        logger     = LogManager.getLogger(SurveyQueryService.class);
    }

    @Override
    public CompletionStage<FindSurveyResponsesByIdsReply>
    findSurveyResponsesByIds(FindSurveyResponsesByIdsRequest request)
    {
        logger.debug("findSurveyResponsesByIds({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(
                    valid ->
                        repository
                            .findAllByIdIn(valid.getResponseIds())
                            .values()
                            .stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()))
                .thenApply(
                    found ->
                        new FindSurveyResponsesByIdsReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Found " + found.size() + " survey response(s)")
                            .setResponses(toData(found)))
                .exceptionally(
                    e ->
                        new FindSurveyResponsesByIdsReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to find survey responses: " + e.getMessage())
                            .setException(ExceptionData.of(e)));
    }

    @Override
    public CompletionStage<FindAllSurveyResponsesReply>
    findAllSurveyResponses(FindAllSurveyResponsesRequest request)
    {
        logger.debug("findAllSurveyResponses({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> Objects.requireNonNull(request,"request"),new CurrentThreadExecutor())
                .thenApply(
                    valid ->
                        valid.getStatuses() == null || valid.getStatuses().isEmpty()
                            ? repository.findAllResponses()
                            : repository.findAllByStatusIn(valid.getStatuses()))
                .thenApply(
                    found ->
                        new FindAllSurveyResponsesReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Found " + found.size() + " survey response(s)")
                            .setResponses(toData(found)))
                .exceptionally(
                    e ->
                        new FindAllSurveyResponsesReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to find survey responses: " + e.getMessage())
                            .setException(ExceptionData.of(e)));
    }

    protected FindSurveyResponsesByIdsRequest
    validate(FindSurveyResponsesByIdsRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getResponseIds(),"validation failed: responseIds were not provided");

        if (request.getResponseIds().isEmpty())
            throw new IllegalArgumentException("validation failed: responseIds were empty");

        return request;
    }

    private List<SurveyResponseData>
    toData(List<SurveyResponse> found)
    {
        return found.stream().map(r -> mapper.map(r,SurveyResponseData.class)).collect(Collectors.toList());
    }
}

//////////////////////////////////////////////////////////////////////////////
