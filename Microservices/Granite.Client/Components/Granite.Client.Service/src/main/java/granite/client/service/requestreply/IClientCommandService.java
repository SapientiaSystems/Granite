//////////////////////////////////////////////////////////////////////////////
// IClientCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IClientCommandService
{
    CompletionStage<CreateClientReply>
    createClient(CreateClientRequest request);

    CompletionStage<UpdateClientsReply>
    updateClients(UpdateClientsRequest request);

    CompletionStage<RotateClientSecretReply>
    rotateClientSecret(RotateClientSecretRequest request);

    CompletionStage<RotateClientSecretReply>
    rotateOwnedClientSecret(RotateOwnedClientSecretRequest request);

    CompletionStage<RotateOwnClientSecretReply>
    rotateOwnClientSecret(RotateOwnClientSecretRequest request);

    CompletionStage<DestroyClientsReply>
    destroyClients(DestroyClientsRequest request);

    CompletionStage<VerifyClientCredentialsReply>
    verifyClientCredentials(VerifyClientCredentialsRequest request);
}

//////////////////////////////////////////////////////////////////////////////
