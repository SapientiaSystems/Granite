import {IClientCommandService} from "granite.client.service/RequestReply";

export
interface IClientCommandServiceClient
    extends IClientCommandService
{
    setAuthorization(accessToken: string): IClientCommandServiceClient;

    close(): void;
}
