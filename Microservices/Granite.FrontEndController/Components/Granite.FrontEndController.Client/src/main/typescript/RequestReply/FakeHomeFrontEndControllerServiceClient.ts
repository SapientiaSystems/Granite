//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }                 from "strata.foundation.core/Concurrent";
import { CompletableObservable }                 from "strata.foundation.core/Concurrent";
import type { IFrontEndControllerServiceClient } from "strata.frontendcontroller.client/RequestReply";
import { Guid }                                  from "guid-typescript";
import { Instant }                               from "@js-joda/core";
import type {
    HandleHomeInputRequest,
    HandleHomeInputReply,
    GetHomeOutputRequest,
    GetHomeOutputReply,
    HomeOutput
} from "granite.frontendcontroller.service/RequestReply";
import type { UserData }                             from "granite.user.service/RequestReply";
import type { IHomeFrontEndControllerServiceClient } from "./IHomeFrontEndControllerServiceClient";

const fakeUsers: Array<UserData> =
[
    { userId: 1, foo: "example-1" } as UserData,
    { userId: 2, foo: "example-2" } as UserData,
    { userId: 3, foo: "example-3" } as UserData,
];

export
class FakeHomeFrontEndControllerServiceClient
    implements IHomeFrontEndControllerServiceClient
{
    private readonly output: HomeOutput =
    {
        userId:           1,
        source:           "Home",
        destination:      "HomeView",
        command:          null,
        userName:         "sample.user",
        firstName:        "Sample",
        lastName:         "User",
        emailAddress:     "sample.user@example.com",
        role:             "User",
        totalUserCount:   fakeUsers.length,
        recentUsers:      fakeUsers,
    } as HomeOutput;

    setAuthorization(_accessToken: string): IFrontEndControllerServiceClient
    {
        return this;
    }

    close(): void
    {
    }

    handleHomeInput(request: HandleHomeInputRequest): ICompletionStage<HandleHomeInputReply>
    {
        const reply: HandleHomeInputReply =
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
            new Promise<HandleHomeInputReply>(resolve => setTimeout(() => resolve(reply),100)));
    }

    getHomeOutput(request: GetHomeOutputRequest): ICompletionStage<GetHomeOutputReply>
    {
        const reply: GetHomeOutputReply =
        {
            replyId:              Guid.create().toString(),
            originatingRequestId: request.requestId ?? Guid.create().toString(),
            timestamp:            Instant.now().toString(),
            success:              true,
            successMessage:       "Output retrieved (fake)",
            failureMessage:       "",
            exception:            null as any,
            output:               this.output,
        };

        return CompletableObservable.fromPromise(
            new Promise<GetHomeOutputReply>(resolve => setTimeout(() => resolve(reply),100)));
    }
}

//////////////////////////////////////////////////////////////////////////////
