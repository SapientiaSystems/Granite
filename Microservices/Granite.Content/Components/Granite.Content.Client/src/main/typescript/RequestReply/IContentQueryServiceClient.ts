import {IContentQueryService} from "granite.content.service/RequestReply";

export
interface IContentQueryServiceClient
    extends IContentQueryService
{
    setAuthorization(accessToken: string): IContentQueryServiceClient;

    close(): void;
}
