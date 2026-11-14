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
    private static final String NOT_ROTATED = "not rotated";

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
            return CompletableFuture.completedFuture(describe(input,null,UnauthenticatedClientsOutput.create()));

        List<String> failures = new ArrayList<>();
        String       id       = input.getClientIdToRotate();
        String[]     revealed = new String[2];

        if (id == null || id.isBlank())
        {
            failures.add("client id required");
            return loader.loadAll(failures).thenApply(output -> describe(input,caller.get(),output));
        }

        return
            clients
                .getClientCommandClient()
                .rotateClientSecret(
                    new RotateClientSecretRequest()
                        .setClientId(id)
                        .setSecretExpiresAt(input.getSecretExpiresAt()))
                .thenAccept(reply -> collect(id,reply,failures,revealed))
                .thenCompose(v -> loader.loadAll(failures))
                .thenApply(
                    output ->
                        describe(
                            input,
                            caller.get(),
                            output.setRevealedClientId(revealed[0]).setRevealedClientSecret(revealed[1])));
    }

    protected void
    collect(String id,RotateClientSecretReply reply,List<String> failures,String[] revealed)
    {
        if (reply.isSuccess() && reply.getClientSecret() != null)
        {
            revealed[0] = reply.getClient() == null || reply.getClient().getClientId() == null ? id : reply.getClient().getClientId();
            revealed[1] = reply.getClientSecret();
        }
        else
            failures.add(id + ": " + reason(reply.getFailureMessage()));
    }

    private ClientsOutput
    describe(ClientsInput input,Long userId,ClientsOutput output)
    {
        return
            output
                .setUserId(userId)
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }

    private String
    reason(String failureMessage)
    {
        return failureMessage == null || failureMessage.isBlank() ? NOT_ROTATED : failureMessage;
    }
}

//////////////////////////////////////////////////////////////////////////////
