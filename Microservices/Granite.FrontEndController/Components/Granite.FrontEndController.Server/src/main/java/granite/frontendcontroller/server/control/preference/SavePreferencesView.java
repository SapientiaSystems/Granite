//////////////////////////////////////////////////////////////////////////////
// SavePreferencesView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.preference;

import granite.frontendcontroller.server.application.PreferenceDefinition;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.PreferencesInput;
import granite.frontendcontroller.service.requestreply.PreferencesOutput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.MergeUserValuesReply;
import granite.user.service.requestreply.MergeUserValuesRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SavePreferencesView
    implements IInputAction<PreferencesInput,PreferencesOutput>
{
    private static final String NOT_SAVED = "not saved";

    private final IUserCommandServiceClient commandClient;
    private final SessionRequestContext     context;
    private final PreferencesAssembler      assembler;

    public
    SavePreferencesView(
        IUserQueryServiceClient    queryClient,
        IUserCommandServiceClient  commandClient,
        SessionRequestContext      context,
        List<PreferenceDefinition> definitions)
    {
        this.commandClient = commandClient;
        this.context       = context;
        this.assembler     = new PreferencesAssembler(queryClient,definitions);
    }

    @Override
    public CompletionStage<PreferencesOutput>
    apply(PreferencesInput input)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return CompletableFuture.completedFuture(describe(input,null,assembler.unauthenticated()));

        List<String>       failures = new ArrayList<>();
        Map<String,Object> entries  = assembler.validate(input.getValues(),failures);

        if (!failures.isEmpty() || entries.isEmpty())
            return assembler.read(caller.get(),failures).thenApply(output -> describe(input,caller.get(),output));

        return
            commandClient
                .mergeUserValues(new MergeUserValuesRequest().setUserId(caller.get()).setValues(entries))
                .thenCompose(reply -> afterMerge(reply,caller.get(),entries,failures))
                .thenApply(output -> describe(input,caller.get(),output));
    }

    protected CompletionStage<PreferencesOutput>
    afterMerge(MergeUserValuesReply reply,Long userId,Map<String,Object> entries,List<String> failures)
    {
        if (!reply.isSuccess())
        {
            collectFailure(reply,failures);
            return assembler.read(userId,failures);
        }

        return CompletableFuture.completedFuture(assembler.fromUser(reply.getMergedUser(),entries,failures));
    }

    protected void
    collectFailure(MergeUserValuesReply reply,List<String> failures)
    {
        if (reply.isSuccess())
            return;

        String message = reply.getFailureMessage();

        failures.add(message == null || message.isBlank() ? NOT_SAVED : message);
    }

    private PreferencesOutput
    describe(PreferencesInput input,Long userId,PreferencesOutput output)
    {
        return
            output
                .setUserId(userId)
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }
}

//////////////////////////////////////////////////////////////////////////////
