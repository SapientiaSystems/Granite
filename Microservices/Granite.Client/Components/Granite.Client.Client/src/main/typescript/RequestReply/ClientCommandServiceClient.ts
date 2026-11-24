import {IClientCommandServiceClient} from "./IClientCommandServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    CreateClientReply,
    CreateClientRequest,
    DestroyClientsReply,
    DestroyClientsRequest,
    RotateClientSecretReply,
    RotateClientSecretRequest,
    RotateOwnClientSecretReply,
    RotateOwnClientSecretRequest,
    RotateOwnedClientSecretRequest,
    UpdateClientsReply,
    UpdateClientsRequest,
    VerifyClientCredentialsReply,
    VerifyClientCredentialsRequest
} from "granite.client.service/RequestReply";

export
class ClientCommandServiceClient
    extends AbstractRestClient
    implements IClientCommandServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IClientCommandServiceClient
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

    createClient(request: CreateClientRequest): ICompletionStage<CreateClientReply>
    {
        return this.doPost<CreateClientReply,CreateClientRequest>(
            "create-client",
            this.initializeRequest(request));
    }

    updateClients(request: UpdateClientsRequest): ICompletionStage<UpdateClientsReply>
    {
        return this.doPost<UpdateClientsReply,UpdateClientsRequest>(
            "update-clients",
            this.initializeRequest(request));
    }

    rotateClientSecret(request: RotateClientSecretRequest): ICompletionStage<RotateClientSecretReply>
    {
        return this.doPost<RotateClientSecretReply,RotateClientSecretRequest>(
            "rotate-client-secret",
            this.initializeRequest(request));
    }

    rotateOwnedClientSecret(request: RotateOwnedClientSecretRequest): ICompletionStage<RotateClientSecretReply>
    {
        return this.doPost<RotateClientSecretReply,RotateOwnedClientSecretRequest>(
            "rotate-owned-client-secret",
            this.initializeRequest(request));
    }

    rotateOwnClientSecret(request: RotateOwnClientSecretRequest): ICompletionStage<RotateOwnClientSecretReply>
    {
        return this.doPost<RotateOwnClientSecretReply,RotateOwnClientSecretRequest>(
            "rotate-own-client-secret",
            this.initializeRequest(request));
    }

    destroyClients(request: DestroyClientsRequest): ICompletionStage<DestroyClientsReply>
    {
        return this.doPost<DestroyClientsReply,DestroyClientsRequest>(
            "destroy-clients",
            this.initializeRequest(request));
    }

    verifyClientCredentials(request: VerifyClientCredentialsRequest): ICompletionStage<VerifyClientCredentialsReply>
    {
        return this.doPost<VerifyClientCredentialsReply,VerifyClientCredentialsRequest>(
            "verify-client-credentials",
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
