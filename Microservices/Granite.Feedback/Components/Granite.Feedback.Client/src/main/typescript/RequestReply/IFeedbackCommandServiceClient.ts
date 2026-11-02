import {IFeedbackCommandService} from "granite.feedback.service/RequestReply";

export
interface IFeedbackCommandServiceClient
    extends IFeedbackCommandService
{
    setAuthorization(accessToken: string): IFeedbackCommandServiceClient;

    close(): void;
}
