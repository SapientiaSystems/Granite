//////////////////////////////////////////////////////////////////////////////
// DeleteClientsView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.client.service.requestreply.DestroyClientsReply;
import granite.client.service.requestreply.DestroyClientsRequest;
import granite.frontendcontroller.server.application.GraniteClientServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ClientsInput;
import granite.frontendcontroller.service.requestreply.ClientsOutput;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class DeleteClientsView
    implements IInputAction<ClientsInput,ClientsOutput>
{
    private static final String NOT_DELETED = "not deleted";

    private final GraniteClientServiceClients clients;
    private final ClientsLoader               loader;
    private final SessionRequestContext       context;

    public
    DeleteClientsView(
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
        Set<String>  ids      = new LinkedHashSet<>();

        if (input.getClientIdsToDelete() != null)
            for (String id : input.getClientIdsToDelete())
                if (id != null && !id.isBlank())
                    ids.add(id);

        return
            destroy(ids,failures)
                .thenCompose(v -> loader.loadAll(failures))
                .thenApply(output -> describe(input,caller.get(),output));
    }

    protected CompletionStage<Void>
    destroy(Set<String> ids,List<String> failures)
    {
        if (ids.isEmpty())
            return CompletableFuture.completedFuture(null);

        return
            clients
                .getClientCommandClient()
                .destroyClients(new DestroyClientsRequest().setClientIds(new ArrayList<>(ids)))
                .thenAccept(reply -> collectFailures(ids,reply,failures));
    }

    protected void
    collectFailures(Set<String> requested,DestroyClientsReply reply,List<String> failures)
    {
        if (reply.isSuccess())
            return;

        Set<String> destroyed = new HashSet<>(reply.getClientIds() == null ? List.of() : reply.getClientIds());

        for (String id : requested)
            if (!destroyed.contains(id))
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
        return failureMessage == null || failureMessage.isBlank() ? NOT_DELETED : failureMessage;
    }
}

//////////////////////////////////////////////////////////////////////////////
