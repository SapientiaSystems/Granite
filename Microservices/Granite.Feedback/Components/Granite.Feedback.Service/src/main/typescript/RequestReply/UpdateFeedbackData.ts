import type { FeedbackStatus } from "./FeedbackStatus";

export
interface UpdateFeedbackData
{
    feedbackId?: number;
    status?: FeedbackStatus;
    notes?: string;
}
