import {IContentQueryServiceClient} from "./IContentQueryServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    FindAllContentDocumentsReply,
    FindAllContentDocumentsRequest,
    FindContentDocumentsByKeysReply,
    FindContentDocumentsByKeysRequest,
    SearchContentReply,
    SearchContentRequest
} from "granite.content.service/RequestReply";

export
class ContentQueryServiceClient
    extends AbstractRestClient
    implements IContentQueryServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IContentQueryServiceClient
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

    searchContent(request: SearchContentRequest): ICompletionStage<SearchContentReply>
    {
        return this.doPost<SearchContentReply,SearchContentRequest>(
            "search-content",
            this.initializeRequest(request));
    }

    findContentDocumentsByKeys(request: FindContentDocumentsByKeysRequest): ICompletionStage<FindContentDocumentsByKeysReply>
    {
        return this.doPost<FindContentDocumentsByKeysReply,FindContentDocumentsByKeysRequest>(
            "find-content-documents-by-keys",
            this.initializeRequest(request));
    }

    findAllContentDocuments(request: FindAllContentDocumentsRequest): ICompletionStage<FindAllContentDocumentsReply>
    {
        return this.doPost<FindAllContentDocumentsReply,FindAllContentDocumentsRequest>(
            "find-all-content-documents",
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
