import {ISessionQueryService} from "granite.session.service/RequestReply";

export
interface ISessionQueryServiceClient
    extends ISessionQueryService
{
    setAuthorization(accessToken: string): ISessionQueryServiceClient;

    close(): void;
}
