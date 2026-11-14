import {IClientQueryService} from "granite.client.service/RequestReply";

export
interface IClientQueryServiceClient
    extends IClientQueryService
{
    setAuthorization(accessToken: string): IClientQueryServiceClient;

    close(): void;
}
