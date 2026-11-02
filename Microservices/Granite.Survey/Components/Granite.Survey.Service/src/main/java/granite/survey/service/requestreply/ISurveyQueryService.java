//////////////////////////////////////////////////////////////////////////////
// ISurveyQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface ISurveyQueryService
{
    CompletionStage<FindSurveyResponsesByIdsReply>
    findSurveyResponsesByIds(FindSurveyResponsesByIdsRequest request);

    CompletionStage<FindAllSurveyResponsesReply>
    findAllSurveyResponses(FindAllSurveyResponsesRequest request);
}

//////////////////////////////////////////////////////////////////////////////
