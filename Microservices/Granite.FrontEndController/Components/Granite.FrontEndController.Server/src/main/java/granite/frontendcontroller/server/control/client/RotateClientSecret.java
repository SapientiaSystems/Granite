//////////////////////////////////////////////////////////////////////////////
// RotateClientSecret.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.client.service.requestreply.RotateClientSecretReply;
import granite.client.service.requestreply.RotateClientSecretRequest;
import granite.frontendcontroller.server.application.GraniteClientServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ClientsInput;
import granite.frontendcontroller.service.requestreply.ClientsOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class RotateClientSecret
    implements IInputAction<ClientsInput,ClientsOutput>
{
    private final GraniteClientServiceClients clients;
    private final ClientsLoader               loader;
    private final SessionRequestContext       context;

    public
    RotateClientSecret(
        GraniteClientServiceClients clients,
        ClientsLoader               loader,
        SessionRequestContext       context)
    {
        this.clients = clients;
        this.loader  = loader;
        this.context = context;
    }

    @Override
    public CompletionStage<ClientsOutput>
    apply(ClientsInput input)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return CompletableFuture.completedFuture(loader.describe(UnauthenticatedClientsOutput.create(),null,input));

        List<String> failures = new ArrayList<>();
        String       id       = input.getClientIdToRotate();
        String[]     revealed = new String[2];

        if (id == null || id.isBlank())
        {
            failures.add("client id required");
            return loader.loadAll(failures).thenApply(output -> loader.describe(output,caller.get(),input));
        }

        return
            clients
                .getClientCommandClient()
                .rotateClientSecret(
                    new RotateClientSecretRequest()
                        .setClientId(id)
                        .setSecretExpiresAt(input.getSecretExpiresAt()))
                .thenAccept(reply -> loader.collectReveal(id,reply,failures,revealed))
                .thenCompose(v -> loader.loadAll(failures,revealed))
                .thenApply(output -> loader.describe(output,caller.get(),input));
    }
}

//////////////////////////////////////////////////////////////////////////////
