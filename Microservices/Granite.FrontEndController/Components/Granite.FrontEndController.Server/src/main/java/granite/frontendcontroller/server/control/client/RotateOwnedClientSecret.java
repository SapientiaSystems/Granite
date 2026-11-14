//////////////////////////////////////////////////////////////////////////////
// RotateOwnedClientSecret.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.client.service.requestreply.RotateClientSecretReply;
import granite.client.service.requestreply.RotateOwnedClientSecretRequest;
import granite.frontendcontroller.server.application.GraniteClientServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ClientsOutput;
import granite.frontendcontroller.service.requestreply.OwnedClientSecretInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class RotateOwnedClientSecret
    implements IInputAction<OwnedClientSecretInput,ClientsOutput>
{
    private final GraniteClientServiceClients clients;
    private final ClientsLoader               loader;
    private final SessionRequestContext       context;

    public
    RotateOwnedClientSecret(
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
    apply(OwnedClientSecretInput input)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return CompletableFuture.completedFuture(loader.describe(UnauthenticatedClientsOutput.create(),null,input));

        List<String> failures = new ArrayList<>();
        String       id       = input.getClientId();
        String[]     revealed = new String[2];

        if (id == null || id.isBlank())
        {
            failures.add("client id required");
            return loader.loadOwned(caller.get(),failures).thenApply(output -> loader.describe(output,caller.get(),input));
        }

        return
            clients
                .getClientCommandClient()
                .rotateOwnedClientSecret(new RotateOwnedClientSecretRequest().setClientId(id).setOwnerUserId(caller.get()))
                .thenAccept(reply -> loader.collectReveal(id,reply,failures,revealed))
                .thenCompose(v -> loader.loadOwned(caller.get(),failures,revealed))
                .thenApply(output -> loader.describe(output,caller.get(),input));
    }
}

//////////////////////////////////////////////////////////////////////////////
