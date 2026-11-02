import {IAgentQueryService} from "granite.agent.service/RequestReply";

export
interface IAgentQueryServiceClient
    extends IAgentQueryService
{
    setAuthorization(accessToken: string): IAgentQueryServiceClient;

    close(): void;
}
