//////////////////////////////////////////////////////////////////////////////
// InitializePreferencesView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.preference;

import granite.frontendcontroller.server.application.PreferenceDefinition;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputRequest;
import granite.frontendcontroller.service.requestreply.PreferencesOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializePreferencesView
    implements IOutputAction<GetPreferencesOutputRequest,PreferencesOutput>
{
    private final SessionRequestContext context;
    private final PreferencesAssembler  assembler;

    public
    InitializePreferencesView(
        IUserQueryServiceClient    queryClient,
        SessionRequestContext      context,
        List<PreferenceDefinition> definitions)
    {
        this.context   = context;
        this.assembler = new PreferencesAssembler(queryClient,definitions);
    }

    @Override
    public CompletionStage<PreferencesOutput>
    apply(GetPreferencesOutputRequest request)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return CompletableFuture.completedFuture(describe(request,null,assembler.unauthenticated()));

        return assembler.read(caller.get(),new ArrayList<>()).thenApply(output -> describe(request,caller.get(),output));
    }

    private PreferencesOutput
    describe(GetPreferencesOutputRequest request,Long userId,PreferencesOutput output)
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
