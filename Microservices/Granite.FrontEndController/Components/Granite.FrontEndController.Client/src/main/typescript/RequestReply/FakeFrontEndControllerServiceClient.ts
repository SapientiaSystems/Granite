//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { IFrontEndControllerServiceClient } from "strata.frontendcontroller.client/RequestReply";
import type { ICompletionStage }                 from "strata.foundation.core/Concurrent";
import { CompletableObservable }                 from "strata.foundation.core/Concurrent";
import { Guid }                                  from "guid-typescript";
import { Instant }                               from "@js-joda/core";
import type {
    FrontEndOutput,
    HandleInputRequest,
    HandleInputReply,
    GetOutputRequest,
    GetOutputReply
} from "strata.frontendcontroller.service/RequestReply";
import type {
    HomeOutput,
    UsersOutput
} from "granite.frontendcontroller.service/RequestReply";
import type { UserData } from "granite.user.service/RequestReply";

const fakeUsers: Array<UserData> =
[
    { userId: 1, foo: "example-1" } as UserData,
    { userId: 2, foo: "example-2" } as UserData,
    { userId: 3, foo: "example-3" } as UserData,
];

export
class FakeFrontEndControllerServiceClient
    implements IFrontEndControllerServiceClient
{
    private outputs: Map<string, FrontEndOutput>;

    constructor()
    {
        this.outputs = new Map<string, FrontEndOutput>();

        this.outputs.set("Home", {
            userId:                 1,
            source:                 "Home",
            destination:            "HomeView",
            command:                null,
            userName:               "sample.user",
            firstName:              "Sample",
            lastName:               "User",
            emailAddress:           "sample.user@example.com",
            role:                   "User",
            totalUserCount:   fakeUsers.length,
            recentUsers:      fakeUsers,
        } as HomeOutput);

        this.outputs.set("User.All", {
            userId:       1,
            source:       "User.All",
            destination:  "UsersEditableView",
            command:      null,
            users:  fakeUsers,
        } as UsersOutput);
    }

    getOutputs(): Map<string, FrontEndOutput>
    {
        return this.outputs;
    }

    setAuthorization(_accessToken: string): IFrontEndControllerServiceClient
    {
        return this;
    }

    close(): void
    {
    }

    handleInput(request: HandleInputRequest): ICompletionStage<HandleInputReply>
    {
        const reply: HandleInputReply =
        {
            replyId:              Guid.create().toString(),
            originatingRequestId: request.requestId ?? Guid.create().toString(),
            timestamp:            Instant.now().toString(),
            success:              true,
            successMessage:       "Handled (fake)",
            failureMessage:       "",
            exception:            null as any,
            input:                request.input ?? null as any,
            output:               null as any,
        };

        return CompletableObservable.fromPromise(
            new Promise<HandleInputReply>(resolve => setTimeout(() => resolve(reply), 100)));
    }

    getOutput(request: GetOutputRequest): ICompletionStage<GetOutputReply>
    {
        const output: FrontEndOutput = this.outputs.get(request.source) ??
        {
            userId:      request.userId,
            source:      request.source,
            destination: request.destination,
            command:     null,
        };

        const reply: GetOutputReply =
        {
            replyId:              Guid.create().toString(),
            originatingRequestId: request.requestId ?? Guid.create().toString(),
            timestamp:            Instant.now().toString(),
            success:              true,
            successMessage:       "Output retrieved (fake)",
            failureMessage:       "",
            exception:            null as any,
            output:               output,
        };

        return CompletableObservable.fromPromise(
            new Promise<GetOutputReply>(resolve => setTimeout(() => resolve(reply), 100)));
    }
}

//////////////////////////////////////////////////////////////////////////////
