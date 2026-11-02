import {ISurveyCommandServiceClient} from "./ISurveyCommandServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    MarkSurveyResponsesReceivedReply,
    MarkSurveyResponsesReceivedRequest,
    MarkSurveyResponsesSentReply,
    MarkSurveyResponsesSentRequest,
    SubmitSurveyResponseReply,
    SubmitSurveyResponseRequest,
    UpdateSurveyResponsesReply,
    UpdateSurveyResponsesRequest
} from "granite.survey.service/RequestReply";

export
class SurveyCommandServiceClient
    extends AbstractRestClient
    implements ISurveyCommandServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): ISurveyCommandServiceClient
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

    submitSurveyResponse(request: SubmitSurveyResponseRequest): ICompletionStage<SubmitSurveyResponseReply>
    {
        return this.doPost<SubmitSurveyResponseReply,SubmitSurveyResponseRequest>(
            "submit-survey-response",
            this.initializeRequest(request));
    }

    updateSurveyResponses(request: UpdateSurveyResponsesRequest): ICompletionStage<UpdateSurveyResponsesReply>
    {
        return this.doPost<UpdateSurveyResponsesReply,UpdateSurveyResponsesRequest>(
            "update-survey-responses",
            this.initializeRequest(request));
    }

    markSurveyResponsesSent(request: MarkSurveyResponsesSentRequest): ICompletionStage<MarkSurveyResponsesSentReply>
    {
        return this.doPost<MarkSurveyResponsesSentReply,MarkSurveyResponsesSentRequest>(
            "mark-survey-responses-sent",
            this.initializeRequest(request));
    }

    markSurveyResponsesReceived(request: MarkSurveyResponsesReceivedRequest): ICompletionStage<MarkSurveyResponsesReceivedReply>
    {
        return this.doPost<MarkSurveyResponsesReceivedReply,MarkSurveyResponsesReceivedRequest>(
            "mark-survey-responses-received",
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
