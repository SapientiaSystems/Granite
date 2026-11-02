import {IAgentCommandService} from "granite.agent.service/RequestReply";

export
interface IAgentCommandServiceClient
    extends IAgentCommandService
{
    setAuthorization(accessToken: string): IAgentCommandServiceClient;

    close(): void;
}
