import {IUserCommandService} from "granite.user.service/RequestReply";

export
interface IUserCommandServiceClient
    extends IUserCommandService
{
    setAuthorization(accessToken: string): IUserCommandServiceClient;

    close(): void;
}

