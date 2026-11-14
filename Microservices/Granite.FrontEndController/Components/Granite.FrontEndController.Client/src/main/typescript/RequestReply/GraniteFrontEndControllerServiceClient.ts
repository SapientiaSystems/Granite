//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }                          from "strata.foundation.core/Concurrent";
import type { HandleUsersInputRequest, HandleUsersInputReply,
              GetUsersOutputRequest, GetUsersOutputReply,
              HandleFeedbackInputRequest, HandleFeedbackInputReply,
              HandleAgentInputRequest, HandleAgentInputReply,
              GetAgentOutputRequest, GetAgentOutputReply,
              HandleContentInputRequest, HandleContentInputReply,
              GetContentOutputRequest, GetContentOutputReply,
              HandleSurveyInputRequest, HandleSurveyInputReply,
              GetFeedbackOutputRequest, GetFeedbackOutputReply,
              GetSurveyOutputRequest, GetSurveyOutputReply,
              HandleFeedbackReviewInputRequest, HandleFeedbackReviewInputReply,
              HandleSurveyReviewInputRequest, HandleSurveyReviewInputReply,
              GetFeedbackReviewOutputRequest, GetFeedbackReviewOutputReply,
              GetSurveyReviewOutputRequest, GetSurveyReviewOutputReply,
              GetHomeOutputRequest, GetHomeOutputReply,
              HandleSessionsInputRequest, HandleSessionsInputReply,
              GetSessionsOutputRequest, GetSessionsOutputReply,
              HandleSessionInputRequest, HandleSessionInputReply,
              GetSessionOutputRequest, GetSessionOutputReply,
              HandlePreferencesInputRequest, HandlePreferencesInputReply,
              GetPreferencesOutputRequest, GetPreferencesOutputReply,
              HandleClientsInputRequest, HandleClientsInputReply,
              GetClientsOutputRequest, GetClientsOutputReply,
              HandleOwnedClientSecretInputRequest, HandleOwnedClientSecretInputReply,
              GetOwnedClientsOutputRequest, GetOwnedClientsOutputReply }  from "granite.frontendcontroller.service/RequestReply";
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

    handleAgentInput(request: HandleAgentInputRequest): ICompletionStage<HandleAgentInputReply>
    {
        return this.doPost<HandleAgentInputReply,HandleAgentInputRequest>(
            "handle-agent-input",
            this.initializeRequest(request));
    }

    getAgentOutput(request: GetAgentOutputRequest): ICompletionStage<GetAgentOutputReply>
    {
        return this.doPost<GetAgentOutputReply,GetAgentOutputRequest>(
            "get-agent-output",
            this.initializeRequest(request));
    }

    handleContentInput(request: HandleContentInputRequest): ICompletionStage<HandleContentInputReply>
    {
        return this.doPost<HandleContentInputReply,HandleContentInputRequest>(
            "handle-content-input",
            this.initializeRequest(request));
    }

    getContentOutput(request: GetContentOutputRequest): ICompletionStage<GetContentOutputReply>
    {
        return this.doPost<GetContentOutputReply,GetContentOutputRequest>(
            "get-content-output",
            this.initializeRequest(request));
    }

    handleSurveyInput(request: HandleSurveyInputRequest): ICompletionStage<HandleSurveyInputReply>
    {
        return this.doPost<HandleSurveyInputReply,HandleSurveyInputRequest>(
            "handle-survey-input",
            this.initializeRequest(request));
    }

    getSurveyOutput(request: GetSurveyOutputRequest): ICompletionStage<GetSurveyOutputReply>
    {
        return this.doPost<GetSurveyOutputReply,GetSurveyOutputRequest>(
            "get-survey-output",
            this.initializeRequest(request));
    }

    handleSurveyReviewInput(request: HandleSurveyReviewInputRequest): ICompletionStage<HandleSurveyReviewInputReply>
    {
        return this.doPost<HandleSurveyReviewInputReply,HandleSurveyReviewInputRequest>(
            "handle-survey-review-input",
            this.initializeRequest(request));
    }

    getSurveyReviewOutput(request: GetSurveyReviewOutputRequest): ICompletionStage<GetSurveyReviewOutputReply>
    {
        return this.doPost<GetSurveyReviewOutputReply,GetSurveyReviewOutputRequest>(
            "get-survey-review-output",
            this.initializeRequest(request));
    }

    getHomeOutput(request: GetHomeOutputRequest): ICompletionStage<GetHomeOutputReply>
    {
        return this.doPost<GetHomeOutputReply,GetHomeOutputRequest>(
            "get-home-output",
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

    handleClientsInput(request: HandleClientsInputRequest): ICompletionStage<HandleClientsInputReply>
    {
        return this.doPost<HandleClientsInputReply,HandleClientsInputRequest>(
            "handle-clients-input",
            this.initializeRequest(request));
    }

    getClientsOutput(request: GetClientsOutputRequest): ICompletionStage<GetClientsOutputReply>
    {
        return this.doPost<GetClientsOutputReply,GetClientsOutputRequest>(
            "get-clients-output",
            this.initializeRequest(request));
    }

    handleOwnedClientSecretInput(request: HandleOwnedClientSecretInputRequest): ICompletionStage<HandleOwnedClientSecretInputReply>
    {
        return this.doPost<HandleOwnedClientSecretInputReply,HandleOwnedClientSecretInputRequest>(
            "handle-owned-client-secret-input",
            this.initializeRequest(request));
    }

    getOwnedClientsOutput(request: GetOwnedClientsOutputRequest): ICompletionStage<GetOwnedClientsOutputReply>
    {
        return this.doPost<GetOwnedClientsOutputReply,GetOwnedClientsOutputRequest>(
            "get-owned-clients-output",
            this.initializeRequest(request));
    }
}

//////////////////////////////////////////////////////////////////////////////
