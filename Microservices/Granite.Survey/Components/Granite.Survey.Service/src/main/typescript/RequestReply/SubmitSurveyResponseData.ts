export
interface SubmitSurveyResponseData
{
    userId?: number;
    answers?: Record<string,number>;
    comment?: string;
}
