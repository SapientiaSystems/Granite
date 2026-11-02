import {IFeedbackQueryService} from "granite.feedback.service/RequestReply";

export
interface IFeedbackQueryServiceClient
    extends IFeedbackQueryService
{
    setAuthorization(accessToken: string): IFeedbackQueryServiceClient;

    close(): void;
}
