//////////////////////////////////////////////////////////////////////////////
// CloseSessionsView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.sessions;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.control.user.RoleCatalogData;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SessionsInput;
import granite.frontendcontroller.service.requestreply.SessionsOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.CloseSessionsReply;
import granite.session.service.requestreply.CloseSessionsRequest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class CloseSessionsView
    implements IInputAction<SessionsInput,SessionsOutput>
{
    private static final String NOT_ENDED = "not ended";

    private final ISessionCommandServiceClient commandClient;
    private final SessionsLoader               loader;
    private final SessionRequestContext        context;
    private final ApplicationRolesManager      applicationRoles;

    public
    CloseSessionsView(
        ISessionCommandServiceClient commandClient,
        SessionsLoader               loader,
        SessionRequestContext        context,
        ApplicationRolesManager      applicationRoles)
    {
        this.commandClient    = commandClient;
        this.loader           = loader;
        this.context          = context;
        this.applicationRoles = applicationRoles;
    }

    @Override
    public CompletionStage<SessionsOutput>
    apply(SessionsInput input)
    {
        if (context.getCallerUserId().isEmpty())
            return CompletableFuture.completedFuture(describe(input,UnauthenticatedSessionsOutput.create()));

        Optional<Long> current  = context.getSessionId();
        List<String>   failures = new ArrayList<>();
        Set<Long>      ids      = new LinkedHashSet<>();

        for (Long id : input.getSessionIdsToClose() == null ? List.<Long>of() : input.getSessionIdsToClose())
        {
            if (id == null)
                continue;

            if (current.isPresent() && Objects.equals(id,current.get()))
                failures.add(id + ": cannot end your own session");
            else
                ids.add(id);
        }

        return
            close(ids,failures)
                .thenCompose(v -> loader.load(input.getSource(),failures))
                .thenApply(
                    rows ->
                        describe(
                            input,
                            new SessionsOutput()
                                .setSessions(rows)
                                .setCurrentSessionId(current.orElse(null))
                                .setFailureMessages(failures)));
    }

    protected CompletionStage<Void>
    close(Set<Long> ids,List<String> failures)
    {
        if (ids.isEmpty())
            return CompletableFuture.completedFuture(null);

        return
            commandClient
                .closeSessions(new CloseSessionsRequest().setSessionIds(ids))
                .thenAccept(reply -> collectFailures(reply,failures));
    }

    protected void
    collectFailures(CloseSessionsReply reply,List<String> failures)
    {
        if (reply == null)
        {
            failures.add(NOT_ENDED);
            return;
        }

        Set<Long> failed = reply.getFailedSessionIds();

        if (failed != null && !failed.isEmpty())
        {
            for (Long id : failed)
                failures.add(id + ": " + NOT_ENDED);
        }
        else if (!reply.isSuccess())
            failures.add(reason(reply.getFailureMessage()));
    }

    private SessionsOutput
    describe(SessionsInput input,SessionsOutput output)
    {
        return
            output
                .setUserId(context.getCallerUserId().orElse(null))
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null))
                .setRoles(RoleCatalogData.from(applicationRoles));
    }

    private String
    reason(String failureMessage)
    {
        return failureMessage == null || failureMessage.isBlank() ? NOT_ENDED : failureMessage;
    }
}

//////////////////////////////////////////////////////////////////////////////
