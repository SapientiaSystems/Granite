import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { FindFeedbackByIdsRequest } from "./FindFeedbackByIdsRequest";
import type { FindFeedbackByIdsReply } from "./FindFeedbackByIdsReply";
import type { FindAllFeedbackRequest } from "./FindAllFeedbackRequest";
import type { FindAllFeedbackReply } from "./FindAllFeedbackReply";

export
interface IFeedbackQueryService
{
    findFeedbackByIds(request: FindFeedbackByIdsRequest): ICompletionStage<FindFeedbackByIdsReply>;

    findAllFeedback(request: FindAllFeedbackRequest): ICompletionStage<FindAllFeedbackReply>;
}
