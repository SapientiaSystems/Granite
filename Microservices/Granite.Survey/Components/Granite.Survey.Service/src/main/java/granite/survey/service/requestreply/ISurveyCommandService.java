//////////////////////////////////////////////////////////////////////////////
// ISurveyCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface ISurveyCommandService
{
    CompletionStage<SubmitSurveyResponseReply>
    submitSurveyResponse(SubmitSurveyResponseRequest request);

    CompletionStage<UpdateSurveyResponsesReply>
    updateSurveyResponses(UpdateSurveyResponsesRequest request);

    CompletionStage<MarkSurveyResponsesSentReply>
    markSurveyResponsesSent(MarkSurveyResponsesSentRequest request);

    CompletionStage<MarkSurveyResponsesReceivedReply>
    markSurveyResponsesReceived(MarkSurveyResponsesReceivedRequest request);
}

//////////////////////////////////////////////////////////////////////////////
