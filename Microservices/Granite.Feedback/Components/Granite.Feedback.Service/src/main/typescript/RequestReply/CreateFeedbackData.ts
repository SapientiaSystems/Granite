import type { FeedbackType } from "./FeedbackType";

export
interface CreateFeedbackData
{
    userId?: number;
    type?: FeedbackType;
    summary?: string;
    details?: Record<string,string>;
}
