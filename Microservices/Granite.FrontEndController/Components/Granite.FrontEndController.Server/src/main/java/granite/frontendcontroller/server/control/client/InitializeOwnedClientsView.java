//////////////////////////////////////////////////////////////////////////////
// InitializeOwnedClientsView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ClientsOutput;
import granite.frontendcontroller.service.requestreply.GetOwnedClientsOutputRequest;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeOwnedClientsView
    implements IOutputAction<GetOwnedClientsOutputRequest,ClientsOutput>
{
    private final ClientsLoader         loader;
    private final SessionRequestContext context;

    public
    InitializeOwnedClientsView(ClientsLoader loader,SessionRequestContext context)
    {
        this.loader  = loader;
        this.context = context;
    }

    @Override
    public CompletionStage<ClientsOutput>
    apply(GetOwnedClientsOutputRequest request)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return CompletableFuture.completedFuture(loader.describe(UnauthenticatedClientsOutput.create(),null,request));

        return
            loader
                .loadOwned(caller.get(),new ArrayList<>())
                .thenApply(output -> loader.describe(output,caller.get(),request));
    }
}

//////////////////////////////////////////////////////////////////////////////
