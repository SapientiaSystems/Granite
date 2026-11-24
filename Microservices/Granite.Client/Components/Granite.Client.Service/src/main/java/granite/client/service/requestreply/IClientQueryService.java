//////////////////////////////////////////////////////////////////////////////
// IClientQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IClientQueryService
{
    CompletionStage<FindClientsReply>
    findAllClients(FindAllClientsRequest request);

    CompletionStage<FindClientsReply>
    findClientsByIds(FindClientsByIdsRequest request);

    CompletionStage<FindClientsReply>
    findClientsByOwner(FindClientsByOwnerRequest request);
}

//////////////////////////////////////////////////////////////////////////////
