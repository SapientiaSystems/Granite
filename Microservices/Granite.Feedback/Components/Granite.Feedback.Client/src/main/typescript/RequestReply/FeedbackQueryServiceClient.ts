import {IFeedbackQueryServiceClient} from "./IFeedbackQueryServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    FindAllFeedbackReply,
    FindAllFeedbackRequest,
    FindFeedbackByIdsReply,
    FindFeedbackByIdsRequest
} from "granite.feedback.service/RequestReply";

export
class FeedbackQueryServiceClient
    extends AbstractRestClient
    implements IFeedbackQueryServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IFeedbackQueryServiceClient
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

    findFeedbackByIds(request: FindFeedbackByIdsRequest): ICompletionStage<FindFeedbackByIdsReply>
    {
        return this.doPost<FindFeedbackByIdsReply,FindFeedbackByIdsRequest>(
            "find-feedback-by-ids",
            this.initializeRequest(request));
    }

    findAllFeedback(request: FindAllFeedbackRequest): ICompletionStage<FindAllFeedbackReply>
    {
        return this.doPost<FindAllFeedbackReply,FindAllFeedbackRequest>(
            "find-all-feedback",
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
