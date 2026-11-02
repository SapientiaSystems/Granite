import {ISurveyQueryService} from "granite.survey.service/RequestReply";

export
interface ISurveyQueryServiceClient
    extends ISurveyQueryService
{
    setAuthorization(accessToken: string): ISurveyQueryServiceClient;

    close(): void;
}
