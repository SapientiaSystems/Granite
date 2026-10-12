import {ISessionCommandService} from "granite.session.service/RequestReply";

export
interface ISessionCommandServiceClient
    extends ISessionCommandService
{
    setAuthorization(accessToken: string): ISessionCommandServiceClient;

    close(): void;
}
