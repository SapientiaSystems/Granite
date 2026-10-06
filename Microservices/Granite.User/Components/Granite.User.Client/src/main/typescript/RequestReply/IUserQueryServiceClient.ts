import {IUserQueryService} from "granite.user.service/RequestReply";

export
interface IUserQueryServiceClient
    extends IUserQueryService
{
    setAuthorization(accessToken: string): IUserQueryServiceClient;

    close(): void;
}

