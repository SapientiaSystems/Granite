import {IUserQueryServiceClient} from "./IUserQueryServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    FindUsersByKeyReply,
    FindUsersByKeyRequest,
    FindUsersByKeySetReply,
    FindUsersByKeySetRequest
} from "granite.user.service/RequestReply";

export
class UserQueryServiceClient
    extends AbstractRestClient
    implements IUserQueryServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IUserQueryServiceClient
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

    findUsersByKey(request: FindUsersByKeyRequest): ICompletionStage<FindUsersByKeyReply>
    {
        console.log("UserQueryServiceClient.findUsersByKey");
        return this.doPost<FindUsersByKeyReply,FindUsersByKeyRequest>(
            "find-users-by-key",
            this.initializeRequest(request));
    }

    findUsersByKeySet(request: FindUsersByKeySetRequest): ICompletionStage<FindUsersByKeySetReply>
    {
        console.log("UserQueryServiceClient.findUsersByKeySet");
        return this.doPost<FindUsersByKeySetReply,FindUsersByKeySetRequest>(
            "find-users-by-keyset",
            this.initializeRequest(request));
    }

    private initializeRequest<R extends AbstractServiceRequest>(request: R): R
    {
        console.log("UserQueryServiceClient.initializeRequest");
        if (request.requestId == null)
            request.requestId = Guid.create().toString();

        if (request.timestamp == null)
            request.timestamp = Instant.now().toString();

        return request;
    }
}

