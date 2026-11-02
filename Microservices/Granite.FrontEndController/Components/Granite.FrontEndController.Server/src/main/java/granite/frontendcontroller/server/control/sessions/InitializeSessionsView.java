//////////////////////////////////////////////////////////////////////////////
// InitializeSessionsView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.sessions;

import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.GetSessionsOutputRequest;
import granite.frontendcontroller.service.requestreply.SessionsOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeSessionsView
    implements IOutputAction<GetSessionsOutputRequest,SessionsOutput>
{
    private final SessionsLoader        loader;
    private final SessionRequestContext context;

    public
    InitializeSessionsView(SessionsLoader loader,SessionRequestContext context)
    {
        this.loader  = loader;
        this.context = context;
    }

    @Override
    public CompletionStage<SessionsOutput>
    apply(GetSessionsOutputRequest request)
    {
        if (context.getCallerUserId().isEmpty())
            return CompletableFuture.completedFuture(describe(request,UnauthenticatedSessionsOutput.create()));

        List<String> failures = new ArrayList<>();

        return
            loader
                .load(request.getSource(),failures)
                .thenApply(
                    rows ->
                        describe(
                            request,
                            new SessionsOutput()
                                .setSessions(rows)
                                .setCurrentSessionId(context.getSessionId().orElse(null))
                                .setFailureMessages(failures)));
    }

    private SessionsOutput
    describe(GetSessionsOutputRequest request,SessionsOutput output)
    {
        return
            output
                .setUserId(context.getCallerUserId().orElse(null))
                .setSource(request.getSource())
                .setCommand(request.getCommand())
                .setDestination(request.getDestination());
    }
}

//////////////////////////////////////////////////////////////////////////////
