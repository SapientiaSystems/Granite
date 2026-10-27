import {IUserCommandServiceClient} from "./IUserCommandServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    CreateUserReply,
    CreateUserRequest,
    CreateUsersReply,
    CreateUsersRequest,
    UpdateUserReply,
    UpdateUserRequest,
    MergeUserValuesReply,
    MergeUserValuesRequest,
    DestroyUserReply,
    DestroyUserRequest,
    DestroyUsersReply,
    DestroyUsersRequest,
    AuthenticateUserReply,
    AuthenticateUserRequest
} from "granite.user.service/RequestReply";

export
class UserCommandServiceClient
    extends AbstractRestClient
    implements IUserCommandServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IUserCommandServiceClient
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

    createUser(request: CreateUserRequest): ICompletionStage<CreateUserReply>
    {
        console.log("UserCommandServiceClient.createUser");
        return this.doPost<CreateUserReply,CreateUserRequest>(
            "create-user",
            this.initializeRequest(request));
    }

    createUsers(request: CreateUsersRequest): ICompletionStage<CreateUsersReply>
    {
        console.log("UserCommandServiceClient.createUsers");
        return this.doPost<CreateUsersReply,CreateUsersRequest>(
            "create-users",
            this.initializeRequest(request));
    }

    updateUser(request: UpdateUserRequest): ICompletionStage<UpdateUserReply>
    {
        console.log("UserCommandServiceClient.updateUser");
        return this.doPost<UpdateUserReply,UpdateUserRequest>(
            "update-user",
            this.initializeRequest(request));
    }

    mergeUserValues(request: MergeUserValuesRequest): ICompletionStage<MergeUserValuesReply>
    {
        console.log("UserCommandServiceClient.mergeUserValues");
        return this.doPost<MergeUserValuesReply,MergeUserValuesRequest>(
            "merge-user-values",
            this.initializeRequest(request));
    }

    destroyUser(request: DestroyUserRequest): ICompletionStage<DestroyUserReply>
    {
        console.log("UserCommandServiceClient.destroyUser");
        return this.doPost<DestroyUserReply,DestroyUserRequest>(
            "destroy-user",
            this.initializeRequest(request));
    }

    destroyUsers(request: DestroyUsersRequest): ICompletionStage<DestroyUsersReply>
    {
        console.log("UserCommandServiceClient.destroyUsers");
        return this.doPost<DestroyUsersReply,DestroyUsersRequest>(
            "destroy-users",
            this.initializeRequest(request));
    }

    authenticateUser(request: AuthenticateUserRequest): ICompletionStage<AuthenticateUserReply>
    {
        console.log("UserCommandServiceClient.authenticateUser");
        return this.doPost<AuthenticateUserReply,AuthenticateUserRequest>(
            "authenticate-user",
            this.initializeRequest(request));
    }

    private initializeRequest<R extends AbstractServiceRequest>(request: R): R
    {
        console.log("UserCommandServiceClient.initializeRequest");
        if (request.requestId == null)
            request.requestId = Guid.create().toString();

        if (request.timestamp == null)
            request.timestamp = Instant.now().toString();

        return request;
    }
}

