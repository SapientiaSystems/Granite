import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { SubmitSurveyResponseRequest } from "./SubmitSurveyResponseRequest";
import type { SubmitSurveyResponseReply } from "./SubmitSurveyResponseReply";
import type { UpdateSurveyResponsesRequest } from "./UpdateSurveyResponsesRequest";
import type { UpdateSurveyResponsesReply } from "./UpdateSurveyResponsesReply";
import type { MarkSurveyResponsesSentRequest } from "./MarkSurveyResponsesSentRequest";
import type { MarkSurveyResponsesSentReply } from "./MarkSurveyResponsesSentReply";
import type { MarkSurveyResponsesReceivedRequest } from "./MarkSurveyResponsesReceivedRequest";
import type { MarkSurveyResponsesReceivedReply } from "./MarkSurveyResponsesReceivedReply";

export
interface ISurveyCommandService
{
    submitSurveyResponse(request: SubmitSurveyResponseRequest): ICompletionStage<SubmitSurveyResponseReply>;

    updateSurveyResponses(request: UpdateSurveyResponsesRequest): ICompletionStage<UpdateSurveyResponsesReply>;

    markSurveyResponsesSent(request: MarkSurveyResponsesSentRequest): ICompletionStage<MarkSurveyResponsesSentReply>;

    markSurveyResponsesReceived(request: MarkSurveyResponsesReceivedRequest): ICompletionStage<MarkSurveyResponsesReceivedReply>;
}
