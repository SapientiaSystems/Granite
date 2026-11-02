import type { SurveyResponseStatus } from "./SurveyResponseStatus";

export
interface UpdateSurveyResponseData
{
    responseId?: number;
    status?: SurveyResponseStatus;
    notes?: string;
}
