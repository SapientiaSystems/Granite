import {ISurveyQueryServiceClient} from "./ISurveyQueryServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    FindAllSurveyResponsesReply,
    FindAllSurveyResponsesRequest,
    FindSurveyResponsesByIdsReply,
    FindSurveyResponsesByIdsRequest
} from "granite.survey.service/RequestReply";

export
class SurveyQueryServiceClient
    extends AbstractRestClient
    implements ISurveyQueryServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): ISurveyQueryServiceClient
    {
        if (super.hasHeader("Authorization"))
            super.clearHeader("Authorization");

        super.setHeader("Authorization","Bearer " + accessToken);
        return this;
    }

    close(): void
    {
        super.clearHeaders();
    }

    findSurveyResponsesByIds(request: FindSurveyResponsesByIdsRequest): ICompletionStage<FindSurveyResponsesByIdsReply>
    {
        return this.doPost<FindSurveyResponsesByIdsReply,FindSurveyResponsesByIdsRequest>(
            "find-survey-responses-by-ids",
            this.initializeRequest(request));
    }

    findAllSurveyResponses(request: FindAllSurveyResponsesRequest): ICompletionStage<FindAllSurveyResponsesReply>
    {
        return this.doPost<FindAllSurveyResponsesReply,FindAllSurveyResponsesRequest>(
            "find-all-survey-responses",
            this.initializeRequest(request));
    }

    private initializeRequest<R extends AbstractServiceRequest>(request: R): R
    {
        if (request.requestId == null)
            request.requestId = Guid.create().toString();

        if (request.timestamp == null)
            request.timestamp = Instant.now().toString();

        return request;
    }
}
