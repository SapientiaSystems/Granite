//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }                          from "strata.foundation.core/Concurrent";
import type { HandleUsersInputRequest, HandleUsersInputReply,
              GetUsersOutputRequest, GetUsersOutputReply,
              HandleFeedbackInputRequest, HandleFeedbackInputReply,
              GetFeedbackOutputRequest, GetFeedbackOutputReply,
              HandleFeedbackReviewInputRequest, HandleFeedbackReviewInputReply,
              GetFeedbackReviewOutputRequest, GetFeedbackReviewOutputReply,
              HandleSessionsInputRequest, HandleSessionsInputReply,
              GetSessionsOutputRequest, GetSessionsOutputReply,
              HandleSessionInputRequest, HandleSessionInputReply,
              GetSessionOutputRequest, GetSessionOutputReply,
              HandlePreferencesInputRequest, HandlePreferencesInputReply,
              GetPreferencesOutputRequest, GetPreferencesOutputReply }  from "granite.frontendcontroller.service/RequestReply";
import { AbstractGraniteFrontEndControllerServiceClient } from "./AbstractGraniteFrontEndControllerServiceClient";
import type { FetchFunction }                             from "./FetchFunction";
import type { IGraniteFrontEndControllerServiceClient }   from "./IGraniteFrontEndControllerServiceClient";

export
class GraniteFrontEndControllerServiceClient
    extends AbstractGraniteFrontEndControllerServiceClient
    implements IGraniteFrontEndControllerServiceClient
{
    constructor(baseUrl: string,fetchFunction?: FetchFunction)
    {
        super(baseUrl,fetchFunction);
    }

    handleUsersInput(request: HandleUsersInputRequest): ICompletionStage<HandleUsersInputReply>
    {
        return this.doPost<HandleUsersInputReply,HandleUsersInputRequest>(
            "handle-users-input",
            this.initializeRequest(request));
    }

    getUsersOutput(request: GetUsersOutputRequest): ICompletionStage<GetUsersOutputReply>
    {
        return this.doPost<GetUsersOutputReply,GetUsersOutputRequest>(
            "get-users-output",
            this.initializeRequest(request));
    }

    handleFeedbackInput(request: HandleFeedbackInputRequest): ICompletionStage<HandleFeedbackInputReply>
    {
        return this.doPost<HandleFeedbackInputReply,HandleFeedbackInputRequest>(
            "handle-feedback-input",
            this.initializeRequest(request));
    }

    getFeedbackOutput(request: GetFeedbackOutputRequest): ICompletionStage<GetFeedbackOutputReply>
    {
        return this.doPost<GetFeedbackOutputReply,GetFeedbackOutputRequest>(
            "get-feedback-output",
            this.initializeRequest(request));
    }

    handleFeedbackReviewInput(request: HandleFeedbackReviewInputRequest): ICompletionStage<HandleFeedbackReviewInputReply>
    {
        return this.doPost<HandleFeedbackReviewInputReply,HandleFeedbackReviewInputRequest>(
            "handle-feedback-review-input",
            this.initializeRequest(request));
    }

    getFeedbackReviewOutput(request: GetFeedbackReviewOutputRequest): ICompletionStage<GetFeedbackReviewOutputReply>
    {
        return this.doPost<GetFeedbackReviewOutputReply,GetFeedbackReviewOutputRequest>(
            "get-feedback-review-output",
            this.initializeRequest(request));
    }

    handleSessionsInput(request: HandleSessionsInputRequest): ICompletionStage<HandleSessionsInputReply>
    {
        return this.doPost<HandleSessionsInputReply,HandleSessionsInputRequest>(
            "handle-sessions-input",
            this.initializeRequest(request));
    }

    getSessionsOutput(request: GetSessionsOutputRequest): ICompletionStage<GetSessionsOutputReply>
    {
        return this.doPost<GetSessionsOutputReply,GetSessionsOutputRequest>(
            "get-sessions-output",
            this.initializeRequest(request));
    }

    handleSessionInput(request: HandleSessionInputRequest): ICompletionStage<HandleSessionInputReply>
    {
        return this.doPost<HandleSessionInputReply,HandleSessionInputRequest>(
            "handle-session-input",
            this.initializeRequest(request));
    }

    getSessionOutput(request: GetSessionOutputRequest): ICompletionStage<GetSessionOutputReply>
    {
        return this.doPost<GetSessionOutputReply,GetSessionOutputRequest>(
            "get-session-output",
            this.initializeRequest(request));
    }

    handlePreferencesInput(request: HandlePreferencesInputRequest): ICompletionStage<HandlePreferencesInputReply>
    {
        return this.doPost<HandlePreferencesInputReply,HandlePreferencesInputRequest>(
            "handle-preferences-input",
            this.initializeRequest(request));
    }

    getPreferencesOutput(request: GetPreferencesOutputRequest): ICompletionStage<GetPreferencesOutputReply>
    {
        return this.doPost<GetPreferencesOutputReply,GetPreferencesOutputRequest>(
            "get-preferences-output",
            this.initializeRequest(request));
    }
}

//////////////////////////////////////////////////////////////////////////////
