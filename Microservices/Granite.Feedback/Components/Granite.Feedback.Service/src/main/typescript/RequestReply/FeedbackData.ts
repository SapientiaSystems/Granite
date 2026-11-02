import type { FeedbackType } from "./FeedbackType";
import type { FeedbackStatus } from "./FeedbackStatus";

export
interface FeedbackData
{
    feedbackId?: number;
    userId?: number;
    type?: FeedbackType;
    summary?: string;
    details?: Record<string,string>;
    status?: FeedbackStatus;
    notes?: string;
    created?: string;
    updated?: string;
    sent?: string;
    received?: string;
}
