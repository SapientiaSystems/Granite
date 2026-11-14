//////////////////////////////////////////////////////////////////////////////
// InitializeClientsView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.frontendcontroller.server.application.GraniteClientServiceClients;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ClientsOutput;
import granite.frontendcontroller.service.requestreply.GetClientsOutputRequest;
import granite.user.client.requestreply.IUserQueryServiceClient;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeClientsView
    implements IOutputAction<GetClientsOutputRequest,ClientsOutput>
{
    private final ClientsLoader         loader;
    private final SessionRequestContext context;

    public
    InitializeClientsView(
        GraniteClientServiceClients clients,
        IUserQueryServiceClient     userQueryClient,
        SessionRequestContext       context,
        ApplicationRolesManager     applicationRoles)
    {
        this(new ClientsLoader(clients,userQueryClient,applicationRoles),context);
    }

    public
    InitializeClientsView(ClientsLoader loader,SessionRequestContext context)
    {
        this.loader  = loader;
        this.context = context;
    }

    @Override
    public CompletionStage<ClientsOutput>
    apply(GetClientsOutputRequest request)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return CompletableFuture.completedFuture(describe(request,null,UnauthenticatedClientsOutput.create()));

        return
            loader
                .loadAll(new ArrayList<>())
                .thenApply(output -> describe(request,caller.get(),output));
    }

    private ClientsOutput
    describe(GetClientsOutputRequest request,Long userId,ClientsOutput output)
    {
        return
            output
                .setUserId(userId)
                .setSource(request.getSource())
                .setCommand(request.getCommand())
                .setDestination(request.getDestination());
    }
}

//////////////////////////////////////////////////////////////////////////////
