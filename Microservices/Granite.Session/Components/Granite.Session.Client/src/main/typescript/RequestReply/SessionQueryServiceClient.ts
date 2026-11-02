import {ISessionQueryServiceClient} from "./ISessionQueryServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    FindSessionsByKeyReply,
    FindSessionsByKeyRequest,
    FindSessionsByKeySetReply,
    FindSessionsByKeySetRequest,
    FindSessionByUserIdAndDeviceIdReply,
    FindSessionByUserIdAndDeviceIdRequest,
    FindSessionsReply,
    FindSessionsRequest
} from "granite.session.service/RequestReply";

export
class SessionQueryServiceClient
    extends AbstractRestClient
    implements ISessionQueryServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): ISessionQueryServiceClient
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

    findSessionsByKey(request: FindSessionsByKeyRequest): ICompletionStage<FindSessionsByKeyReply>
    {
        console.log("SessionQueryServiceClient.findSessionsByKey");
        return this.doPost<FindSessionsByKeyReply,FindSessionsByKeyRequest>(
            "find-sessions-by-key",
            this.initializeRequest(request));
    }

    findSessionsByKeySet(request: FindSessionsByKeySetRequest): ICompletionStage<FindSessionsByKeySetReply>
    {
        console.log("SessionQueryServiceClient.findSessionsByKeySet");
        return this.doPost<FindSessionsByKeySetReply,FindSessionsByKeySetRequest>(
            "find-sessions-by-keyset",
            this.initializeRequest(request));
    }

    findSessionByUserIdAndDeviceId(request: FindSessionByUserIdAndDeviceIdRequest): ICompletionStage<FindSessionByUserIdAndDeviceIdReply>
    {
        console.log("SessionQueryServiceClient.findSessionByUserIdAndDeviceId");
        return this.doPost<FindSessionByUserIdAndDeviceIdReply,FindSessionByUserIdAndDeviceIdRequest>(
            "find-session-by-user-id-and-device-id",
            this.initializeRequest(request));
    }

    findSessions(request: FindSessionsRequest): ICompletionStage<FindSessionsReply>
    {
        console.log("SessionQueryServiceClient.findSessions");
        return this.doPost<FindSessionsReply,FindSessionsRequest>(
            "find-sessions",
            this.initializeRequest(request));
    }

    private initializeRequest<R extends AbstractServiceRequest>(request: R): R
    {
        console.log("SessionQueryServiceClient.initializeRequest");
        if (request.requestId == null)
            request.requestId = Guid.create().toString();

        if (request.timestamp == null)
            request.timestamp = Instant.now().toString();

        return request;
    }
}
