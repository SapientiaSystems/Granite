//////////////////////////////////////////////////////////////////////////////
// IFeedbackQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IFeedbackQueryService
{
    CompletionStage<FindFeedbackByIdsReply>
    findFeedbackByIds(FindFeedbackByIdsRequest request);

    CompletionStage<FindAllFeedbackReply>
    findAllFeedback(FindAllFeedbackRequest request);
}

//////////////////////////////////////////////////////////////////////////////
