//////////////////////////////////////////////////////////////////////////////
// InitializeHomeView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.home;

import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.GetHomeOutputRequest;
import granite.frontendcontroller.service.requestreply.HomeOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeHomeView
    implements IOutputAction<GetHomeOutputRequest,HomeOutput>
{
    public static final String NOT_AUTHENTICATED = "not authenticated";

    private final HomeMetricsLoader     loader;
    private final SessionRequestContext context;

    public
    InitializeHomeView(HomeMetricsLoader loader,SessionRequestContext context)
    {
        this.loader  = loader;
        this.context = context;
    }

    @Override
    public CompletionStage<HomeOutput>
    apply(GetHomeOutputRequest request)
    {
        CompletionStage<HomeOutput> output =
            context.getCallerUserId().isEmpty()
                ? CompletableFuture.completedFuture(new HomeOutput().setFailureMessages(new ArrayList<>(List.of(NOT_AUTHENTICATED))))
                : loader.load(context.getCallerUserId().get(),context.getCallerAuthorities());

        return
            output.thenApply(
                result ->
                    result
                        .setUserId(context.getCallerUserId().orElse(null))
                        .setSource(request.getSource())
                        .setCommand(request.getCommand())
                        .setDestination(request.getDestination()));
    }
}

//////////////////////////////////////////////////////////////////////////////
