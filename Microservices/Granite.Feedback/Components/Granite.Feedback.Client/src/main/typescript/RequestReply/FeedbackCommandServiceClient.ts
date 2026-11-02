import {IFeedbackCommandServiceClient} from "./IFeedbackCommandServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    CreateFeedbackReply,
    CreateFeedbackRequest,
    MarkFeedbackReceivedReply,
    MarkFeedbackReceivedRequest,
    MarkFeedbackSentReply,
    MarkFeedbackSentRequest,
    UpdateFeedbacksReply,
    UpdateFeedbacksRequest
} from "granite.feedback.service/RequestReply";

export
class FeedbackCommandServiceClient
    extends AbstractRestClient
    implements IFeedbackCommandServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IFeedbackCommandServiceClient
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

    createFeedback(request: CreateFeedbackRequest): ICompletionStage<CreateFeedbackReply>
    {
        return this.doPost<CreateFeedbackReply,CreateFeedbackRequest>(
            "create-feedback",
            this.initializeRequest(request));
    }

    updateFeedbacks(request: UpdateFeedbacksRequest): ICompletionStage<UpdateFeedbacksReply>
    {
        return this.doPost<UpdateFeedbacksReply,UpdateFeedbacksRequest>(
            "update-feedbacks",
            this.initializeRequest(request));
    }

    markFeedbackSent(request: MarkFeedbackSentRequest): ICompletionStage<MarkFeedbackSentReply>
    {
        return this.doPost<MarkFeedbackSentReply,MarkFeedbackSentRequest>(
            "mark-feedback-sent",
            this.initializeRequest(request));
    }

    markFeedbackReceived(request: MarkFeedbackReceivedRequest): ICompletionStage<MarkFeedbackReceivedReply>
    {
        return this.doPost<MarkFeedbackReceivedReply,MarkFeedbackReceivedRequest>(
            "mark-feedback-received",
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
