import {ISessionCommandServiceClient} from "./ISessionCommandServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    LoginReply,
    LoginRequest,
    LoginWithOauth2Reply,
    LoginWithOauth2Request,
    LogoutReply,
    LogoutRequest,
    LogoutAllForUserReply,
    LogoutAllForUserRequest,
    RefreshReply,
    RefreshRequest,
    ResolveSessionReply,
    ResolveSessionRequest
} from "granite.session.service/RequestReply";

export
class SessionCommandServiceClient
    extends AbstractRestClient
    implements ISessionCommandServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): ISessionCommandServiceClient
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

    login(request: LoginRequest): ICompletionStage<LoginReply>
    {
        console.log("SessionCommandServiceClient.login");
        return this.doPost<LoginReply,LoginRequest>(
            "login",
            this.initializeRequest(request));
    }

    loginWithOauth2(request: LoginWithOauth2Request): ICompletionStage<LoginWithOauth2Reply>
    {
        console.log("SessionCommandServiceClient.loginWithOauth2");
        return this.doPost<LoginWithOauth2Reply,LoginWithOauth2Request>(
            "login-with-oauth2",
            this.initializeRequest(request));
    }

    logout(request: LogoutRequest): ICompletionStage<LogoutReply>
    {
        console.log("SessionCommandServiceClient.logout");
        return this.doPost<LogoutReply,LogoutRequest>(
            "logout",
            this.initializeRequest(request));
    }

    logoutAllForUser(request: LogoutAllForUserRequest): ICompletionStage<LogoutAllForUserReply>
    {
        console.log("SessionCommandServiceClient.logoutAllForUser");
        return this.doPost<LogoutAllForUserReply,LogoutAllForUserRequest>(
            "logout-all-for-user",
            this.initializeRequest(request));
    }

    refresh(request: RefreshRequest): ICompletionStage<RefreshReply>
    {
        console.log("SessionCommandServiceClient.refresh");
        return this.doPost<RefreshReply,RefreshRequest>(
            "refresh",
            this.initializeRequest(request));
    }

    resolveSession(request: ResolveSessionRequest): ICompletionStage<ResolveSessionReply>
    {
        console.log("SessionCommandServiceClient.resolveSession");
        return this.doPost<ResolveSessionReply,ResolveSessionRequest>(
            "resolve-session",
            this.initializeRequest(request));
    }

    private initializeRequest<R extends AbstractServiceRequest>(request: R): R
    {
        console.log("SessionCommandServiceClient.initializeRequest");
        if (request.requestId == null)
            request.requestId = Guid.create().toString();

        if (request.timestamp == null)
            request.timestamp = Instant.now().toString();

        return request;
    }
}
