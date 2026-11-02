//////////////////////////////////////////////////////////////////////////////
// IGraniteFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.IFrontEndControllerService;

import java.util.concurrent.CompletionStage;

public
interface IGraniteFrontEndControllerService
    extends IFrontEndControllerService
{
    CompletionStage<HandleUsersInputReply>
    handleUsersInput(HandleUsersInputRequest request);

    CompletionStage<GetUsersOutputReply>
    getUsersOutput(GetUsersOutputRequest request);

    CompletionStage<HandleFeedbackInputReply>
    handleFeedbackInput(HandleFeedbackInputRequest request);

    CompletionStage<GetFeedbackOutputReply>
    getFeedbackOutput(GetFeedbackOutputRequest request);

    CompletionStage<HandleFeedbackReviewInputReply>
    handleFeedbackReviewInput(HandleFeedbackReviewInputRequest request);

    CompletionStage<GetFeedbackReviewOutputReply>
    getFeedbackReviewOutput(GetFeedbackReviewOutputRequest request);

    CompletionStage<HandleSurveyInputReply>
    handleSurveyInput(HandleSurveyInputRequest request);

    CompletionStage<GetSurveyOutputReply>
    getSurveyOutput(GetSurveyOutputRequest request);

    CompletionStage<HandleSurveyReviewInputReply>
    handleSurveyReviewInput(HandleSurveyReviewInputRequest request);

    CompletionStage<GetSurveyReviewOutputReply>
    getSurveyReviewOutput(GetSurveyReviewOutputRequest request);

    CompletionStage<HandleSessionsInputReply>
    handleSessionsInput(HandleSessionsInputRequest request);

    CompletionStage<GetSessionsOutputReply>
    getSessionsOutput(GetSessionsOutputRequest request);

    CompletionStage<HandleSessionInputReply>
    handleSessionInput(HandleSessionInputRequest request);

    CompletionStage<GetSessionOutputReply>
    getSessionOutput(GetSessionOutputRequest request);

    CompletionStage<HandlePreferencesInputReply>
    handlePreferencesInput(HandlePreferencesInputRequest request);

    CompletionStage<GetPreferencesOutputReply>
    getPreferencesOutput(GetPreferencesOutputRequest request);
}

//////////////////////////////////////////////////////////////////////////////
