import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { FindAllClientsRequest } from "./FindAllClientsRequest";
import type { FindClientsByIdsRequest } from "./FindClientsByIdsRequest";
import type { FindClientsByOwnerRequest } from "./FindClientsByOwnerRequest";
import type { FindClientsReply } from "./FindClientsReply";

export
interface IClientQueryService
{
    findAllClients(request: FindAllClientsRequest): ICompletionStage<FindClientsReply>;

    findClientsByIds(request: FindClientsByIdsRequest): ICompletionStage<FindClientsReply>;

    findClientsByOwner(request: FindClientsByOwnerRequest): ICompletionStage<FindClientsReply>;
}
