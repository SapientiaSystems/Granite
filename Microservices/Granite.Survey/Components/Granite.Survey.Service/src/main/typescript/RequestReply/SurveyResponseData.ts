import type { SurveyResponseStatus } from "./SurveyResponseStatus";

export
interface SurveyResponseData
{
    responseId?: number;
    userId?: number;
    answers?: Record<string,number>;
    comment?: string;
    status?: SurveyResponseStatus;
    notes?: string;
    created?: string;
    updated?: string;
    sent?: string;
    received?: string;
}
