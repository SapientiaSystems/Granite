import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { FindSurveyResponsesByIdsRequest } from "./FindSurveyResponsesByIdsRequest";
import type { FindSurveyResponsesByIdsReply } from "./FindSurveyResponsesByIdsReply";
import type { FindAllSurveyResponsesRequest } from "./FindAllSurveyResponsesRequest";
import type { FindAllSurveyResponsesReply } from "./FindAllSurveyResponsesReply";

export
interface ISurveyQueryService
{
    findSurveyResponsesByIds(request: FindSurveyResponsesByIdsRequest): ICompletionStage<FindSurveyResponsesByIdsReply>;

    findAllSurveyResponses(request: FindAllSurveyResponsesRequest): ICompletionStage<FindAllSurveyResponsesReply>;
}
