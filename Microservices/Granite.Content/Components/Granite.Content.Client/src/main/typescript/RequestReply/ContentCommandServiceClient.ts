import {IContentCommandServiceClient} from "./IContentCommandServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    DestroyContentDocumentsReply,
    DestroyContentDocumentsRequest,
    UpsertContentDocumentsReply,
    UpsertContentDocumentsRequest
} from "granite.content.service/RequestReply";

export
class ContentCommandServiceClient
    extends AbstractRestClient
    implements IContentCommandServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IContentCommandServiceClient
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

    upsertContentDocuments(request: UpsertContentDocumentsRequest): ICompletionStage<UpsertContentDocumentsReply>
    {
        return this.doPost<UpsertContentDocumentsReply,UpsertContentDocumentsRequest>(
            "upsert-content-documents",
            this.initializeRequest(request));
    }

    destroyContentDocuments(request: DestroyContentDocumentsRequest): ICompletionStage<DestroyContentDocumentsReply>
    {
        return this.doPost<DestroyContentDocumentsReply,DestroyContentDocumentsRequest>(
            "destroy-content-documents",
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
