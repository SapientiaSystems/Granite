import {IClientQueryServiceClient} from "./IClientQueryServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    FindAllClientsRequest,
    FindClientsByIdsRequest,
    FindClientsByOwnerRequest,
    FindClientsReply
} from "granite.client.service/RequestReply";

export
class ClientQueryServiceClient
    extends AbstractRestClient
    implements IClientQueryServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IClientQueryServiceClient
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

    findAllClients(request: FindAllClientsRequest): ICompletionStage<FindClientsReply>
    {
        return this.doPost<FindClientsReply,FindAllClientsRequest>(
            "find-all-clients",
            this.initializeRequest(request));
    }

    findClientsByIds(request: FindClientsByIdsRequest): ICompletionStage<FindClientsReply>
    {
        return this.doPost<FindClientsReply,FindClientsByIdsRequest>(
            "find-clients-by-ids",
            this.initializeRequest(request));
    }

    findClientsByOwner(request: FindClientsByOwnerRequest): ICompletionStage<FindClientsReply>
    {
        return this.doPost<FindClientsReply,FindClientsByOwnerRequest>(
            "find-clients-by-owner",
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
