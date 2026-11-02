//////////////////////////////////////////////////////////////////////////////
// IFeedbackCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IFeedbackCommandService
{
    CompletionStage<CreateFeedbackReply>
    createFeedback(CreateFeedbackRequest request);

    CompletionStage<UpdateFeedbacksReply>
    updateFeedbacks(UpdateFeedbacksRequest request);

    CompletionStage<MarkFeedbackSentReply>
    markFeedbackSent(MarkFeedbackSentRequest request);

    CompletionStage<MarkFeedbackReceivedReply>
    markFeedbackReceived(MarkFeedbackReceivedRequest request);
}

//////////////////////////////////////////////////////////////////////////////
