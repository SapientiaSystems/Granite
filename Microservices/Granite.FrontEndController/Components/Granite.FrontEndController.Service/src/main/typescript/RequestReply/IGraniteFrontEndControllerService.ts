//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }           from "strata.foundation.core/Concurrent";
import type { IFrontEndControllerService } from "strata.frontendcontroller.service/RequestReply";
import type { HandleUsersInputRequest }    from "./HandleUsersInputRequest";
import type { HandleUsersInputReply }      from "./HandleUsersInputReply";
import type { GetUsersOutputRequest }      from "./GetUsersOutputRequest";
import type { GetUsersOutputReply }        from "./GetUsersOutputReply";
import type { HandleFeedbackInputRequest }         from "./HandleFeedbackInputRequest";
import type { HandleAgentInputRequest }          from "./HandleAgentInputRequest";
import type { HandleAgentInputReply }            from "./HandleAgentInputReply";
import type { GetAgentOutputRequest }            from "./GetAgentOutputRequest";
import type { GetAgentOutputReply }              from "./GetAgentOutputReply";
import type { HandleContentInputRequest }        from "./HandleContentInputRequest";
import type { HandleContentInputReply }          from "./HandleContentInputReply";
import type { GetContentOutputRequest }          from "./GetContentOutputRequest";
import type { GetContentOutputReply }            from "./GetContentOutputReply";
import type { HandleSurveyInputRequest }         from "./HandleSurveyInputRequest";
import type { HandleFeedbackInputReply }           from "./HandleFeedbackInputReply";
import type { HandleSurveyInputReply }           from "./HandleSurveyInputReply";
import type { GetFeedbackOutputRequest }           from "./GetFeedbackOutputRequest";
import type { GetSurveyOutputRequest }           from "./GetSurveyOutputRequest";
import type { GetFeedbackOutputReply }             from "./GetFeedbackOutputReply";
import type { GetSurveyOutputReply }             from "./GetSurveyOutputReply";
import type { HandleFeedbackReviewInputRequest }   from "./HandleFeedbackReviewInputRequest";
import type { HandleSurveyReviewInputRequest }   from "./HandleSurveyReviewInputRequest";
import type { HandleFeedbackReviewInputReply }     from "./HandleFeedbackReviewInputReply";
import type { HandleSurveyReviewInputReply }     from "./HandleSurveyReviewInputReply";
import type { GetFeedbackReviewOutputRequest }     from "./GetFeedbackReviewOutputRequest";
import type { GetSurveyReviewOutputRequest }     from "./GetSurveyReviewOutputRequest";
import type { GetFeedbackReviewOutputReply }       from "./GetFeedbackReviewOutputReply";
import type { GetSurveyReviewOutputReply }       from "./GetSurveyReviewOutputReply";
import type { HandleSessionsInputRequest } from "./HandleSessionsInputRequest";
import type { HandleSessionsInputReply }   from "./HandleSessionsInputReply";
import type { GetSessionsOutputRequest }   from "./GetSessionsOutputRequest";
import type { GetSessionsOutputReply }     from "./GetSessionsOutputReply";
import type { HandleSessionInputRequest }  from "./HandleSessionInputRequest";
import type { HandleSessionInputReply }    from "./HandleSessionInputReply";
import type { GetSessionOutputRequest }    from "./GetSessionOutputRequest";
import type { GetSessionOutputReply }      from "./GetSessionOutputReply";
import type { HandlePreferencesInputRequest } from "./HandlePreferencesInputRequest";
import type { HandlePreferencesInputReply }   from "./HandlePreferencesInputReply";
import type { GetPreferencesOutputRequest }   from "./GetPreferencesOutputRequest";
import type { GetPreferencesOutputReply }     from "./GetPreferencesOutputReply";

export
interface IGraniteFrontEndControllerService
    extends IFrontEndControllerService
{
    handleUsersInput(request: HandleUsersInputRequest): ICompletionStage<HandleUsersInputReply>;

    getUsersOutput(request: GetUsersOutputRequest): ICompletionStage<GetUsersOutputReply>;

    handleFeedbackInput(request: HandleFeedbackInputRequest): ICompletionStage<HandleFeedbackInputReply>;
    handleAgentInput(request: HandleAgentInputRequest): ICompletionStage<HandleAgentInputReply>;

    getAgentOutput(request: GetAgentOutputRequest): ICompletionStage<GetAgentOutputReply>;

    handleContentInput(request: HandleContentInputRequest): ICompletionStage<HandleContentInputReply>;

    getContentOutput(request: GetContentOutputRequest): ICompletionStage<GetContentOutputReply>;

    handleSurveyInput(request: HandleSurveyInputRequest): ICompletionStage<HandleSurveyInputReply>;

    getFeedbackOutput(request: GetFeedbackOutputRequest): ICompletionStage<GetFeedbackOutputReply>;
    getSurveyOutput(request: GetSurveyOutputRequest): ICompletionStage<GetSurveyOutputReply>;

    handleFeedbackReviewInput(request: HandleFeedbackReviewInputRequest): ICompletionStage<HandleFeedbackReviewInputReply>;
    handleSurveyReviewInput(request: HandleSurveyReviewInputRequest): ICompletionStage<HandleSurveyReviewInputReply>;

    getFeedbackReviewOutput(request: GetFeedbackReviewOutputRequest): ICompletionStage<GetFeedbackReviewOutputReply>;
    getSurveyReviewOutput(request: GetSurveyReviewOutputRequest): ICompletionStage<GetSurveyReviewOutputReply>;

    handleSessionsInput(request: HandleSessionsInputRequest): ICompletionStage<HandleSessionsInputReply>;

    getSessionsOutput(request: GetSessionsOutputRequest): ICompletionStage<GetSessionsOutputReply>;

    handleSessionInput(request: HandleSessionInputRequest): ICompletionStage<HandleSessionInputReply>;

    getSessionOutput(request: GetSessionOutputRequest): ICompletionStage<GetSessionOutputReply>;

    handlePreferencesInput(request: HandlePreferencesInputRequest): ICompletionStage<HandlePreferencesInputReply>;

    getPreferencesOutput(request: GetPreferencesOutputRequest): ICompletionStage<GetPreferencesOutputReply>;
}

//////////////////////////////////////////////////////////////////////////////
