import {IContentCommandService} from "granite.content.service/RequestReply";

export
interface IContentCommandServiceClient
    extends IContentCommandService
{
    setAuthorization(accessToken: string): IContentCommandServiceClient;

    close(): void;
}
