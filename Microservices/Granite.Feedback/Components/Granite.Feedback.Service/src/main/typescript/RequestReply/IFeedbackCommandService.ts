import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { CreateFeedbackRequest } from "./CreateFeedbackRequest";
import type { CreateFeedbackReply } from "./CreateFeedbackReply";
import type { UpdateFeedbacksRequest } from "./UpdateFeedbacksRequest";
import type { UpdateFeedbacksReply } from "./UpdateFeedbacksReply";
import type { MarkFeedbackSentRequest } from "./MarkFeedbackSentRequest";
import type { MarkFeedbackSentReply } from "./MarkFeedbackSentReply";
import type { MarkFeedbackReceivedRequest } from "./MarkFeedbackReceivedRequest";
import type { MarkFeedbackReceivedReply } from "./MarkFeedbackReceivedReply";

export
interface IFeedbackCommandService
{
    createFeedback(request: CreateFeedbackRequest): ICompletionStage<CreateFeedbackReply>;

    updateFeedbacks(request: UpdateFeedbacksRequest): ICompletionStage<UpdateFeedbacksReply>;

    markFeedbackSent(request: MarkFeedbackSentRequest): ICompletionStage<MarkFeedbackSentReply>;

    markFeedbackReceived(request: MarkFeedbackReceivedRequest): ICompletionStage<MarkFeedbackReceivedReply>;
}
