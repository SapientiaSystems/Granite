//////////////////////////////////////////////////////////////////////////////
// UpdateClientsView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.client.service.requestreply.ClientData;
import granite.client.service.requestreply.CreateClientData;
import granite.client.service.requestreply.CreateClientReply;
import granite.client.service.requestreply.CreateClientRequest;
import granite.client.service.requestreply.UpdateClientData;
import granite.client.service.requestreply.UpdateClientsReply;
import granite.client.service.requestreply.UpdateClientsRequest;
import granite.frontendcontroller.server.application.GraniteClientServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ClientsInput;
import granite.frontendcontroller.service.requestreply.ClientsOutput;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class UpdateClientsView
    implements IInputAction<ClientsInput,ClientsOutput>
{
    public static final String ONE_CREATE_AT_A_TIME = "only one client can be created at a time";

    private static final String NOT_SAVED = "not saved";

    private final GraniteClientServiceClients clients;
    private final ClientsLoader               loader;
    private final SessionRequestContext       context;

    public
    UpdateClientsView(
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
        String[]     revealed = new String[2];

        return
            create(input.getClientsToCreate(),failures,revealed)
                .thenCompose(v -> update(input.getClientsToUpdate(),failures))
                .thenCompose(v -> loader.loadAll(failures))
                .thenApply(
                    output ->
                        describe(
                            input,
                            caller.get(),
                            output.setRevealedClientId(revealed[0]).setRevealedClientSecret(revealed[1])));
    }

    protected CompletionStage<Void>
    create(List<CreateClientData> toCreate,List<String> failures,String[] revealed)
    {
        if (toCreate == null || toCreate.isEmpty())
            return CompletableFuture.completedFuture(null);

        if (toCreate.size() > 1)
        {
            failures.add(ONE_CREATE_AT_A_TIME);
            return CompletableFuture.completedFuture(null);
        }

        CreateClientData data = toCreate.get(0);

        return
            clients
                .getClientCommandClient()
                .createClient(new CreateClientRequest().setClient(data))
                .thenAccept(reply -> collectCreate(data,reply,failures,revealed));
    }

    protected void
    collectCreate(CreateClientData data,CreateClientReply reply,List<String> failures,String[] revealed)
    {
        if (reply.isSuccess() && reply.getClient() != null && reply.getClientSecret() != null)
        {
            revealed[0] = reply.getClient().getClientId();
            revealed[1] = reply.getClientSecret();
        }
        else
            failures.add(data.getName() + ": " + reason(reply.getFailureMessage()));
    }

    protected CompletionStage<Void>
    update(List<UpdateClientData> toUpdate,List<String> failures)
    {
        if (toUpdate == null || toUpdate.isEmpty())
            return CompletableFuture.completedFuture(null);

        return
            clients
                .getClientCommandClient()
                .updateClients(new UpdateClientsRequest().setClients(toUpdate))
                .thenAccept(reply -> collectUpdate(toUpdate,reply,failures));
    }

    protected void
    collectUpdate(List<UpdateClientData> requested,UpdateClientsReply reply,List<String> failures)
    {
        if (reply.isSuccess())
            return;

        Set<String> updated = new HashSet<>();

        if (reply.getClients() != null)
            for (ClientData client : reply.getClients())
                updated.add(client.getClientId());

        for (UpdateClientData client : requested)
            if (!updated.contains(client.getClientId()))
                failures.add(client.getClientId() + ": " + reason(reply.getFailureMessage()));
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
        return failureMessage == null || failureMessage.isBlank() ? NOT_SAVED : failureMessage;
    }
}

//////////////////////////////////////////////////////////////////////////////
