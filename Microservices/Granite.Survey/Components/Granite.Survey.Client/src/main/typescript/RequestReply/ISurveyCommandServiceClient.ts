import {ISurveyCommandService} from "granite.survey.service/RequestReply";

export
interface ISurveyCommandServiceClient
    extends ISurveyCommandService
{
    setAuthorization(accessToken: string): ISurveyCommandServiceClient;

    close(): void;
}
