//////////////////////////////////////////////////////////////////////////////
// DeleteUsersView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.DestroyUsersReply;
import granite.user.service.requestreply.DestroyUsersRequest;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class DeleteUsersView
    implements IInputAction<UsersInput,UsersOutput>
{
    private static final String NOT_DELETED = "not deleted";

    private final IUserQueryServiceClient   queryClient;
    private final IUserCommandServiceClient commandClient;
    private final SessionRequestContext     context;
    private final ApplicationRolesManager   applicationRoles;

    public
    DeleteUsersView(
        IUserQueryServiceClient   queryClient,
        IUserCommandServiceClient commandClient,
        SessionRequestContext     context,
        ApplicationRolesManager   applicationRoles)
    {
        this.queryClient      = queryClient;
        this.commandClient    = commandClient;
        this.context          = context;
        this.applicationRoles = applicationRoles;
    }

    @Override
    public CompletionStage<UsersOutput>
    apply(UsersInput input)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return
                CompletableFuture.completedFuture(
                    UnauthenticatedUsersOutput
                        .create(applicationRoles)
                        .setSource(input.getSource())
                        .setCommand(input.getCommand())
                        .setDestination(input.getDestination().orElse(null)));

        List<String> failures = new ArrayList<>();
        Set<Long>    ids = new LinkedHashSet<>();

        for (Long id : input.getUserIdsToDelete() == null ? List.<Long>of() : input.getUserIdsToDelete())
        {
            if (Objects.equals(id,caller.get()))
                failures.add(id + ": cannot delete your own user");
            else
                ids.add(id);
        }

        return
            destroy(ids,failures)
                .thenCompose(v -> queryClient.findAllUsers(new FindAllUsersRequest()))
                .thenApply(reply -> toUsersOutput(input,reply,failures));
    }

    protected CompletionStage<Void>
    destroy(Set<Long> ids,List<String> failures)
    {
        if (ids.isEmpty())
            return CompletableFuture.completedFuture(null);

        return
            commandClient
                .destroyUsers(new DestroyUsersRequest().setUserIds(ids))
                .thenAccept(reply -> collectFailures(reply,failures));
    }

    protected void
    collectFailures(DestroyUsersReply reply,List<String> failures)
    {
        Set<Long> failed = reply.getFailedUserIds();

        if (failed != null && !failed.isEmpty())
        {
            for (Long id : failed)
                failures.add(id + ": " + reason(reply.getFailureMessage()));
        }
        else if (!reply.isSuccess())
            failures.add(reason(reply.getFailureMessage()));
    }

    protected UsersOutput
    toUsersOutput(UsersInput input,FindAllUsersReply reply,List<String> failures)
    {
        List<UserData> users = List.of();

        if (reply.isSuccess())
            users = reply.getUsers();
        else
            failures.add(refreshFailure(reply));

        return
            new UsersOutput()
                .setUsers(users)
                .setFailureMessages(failures)
                .setRoles(RoleCatalogData.from(applicationRoles))
                .setUserId(context.getCallerUserId().orElse(null))
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }

    private String
    refreshFailure(FindAllUsersReply reply)
    {
        String message = reply.getFailureMessage();

        return "Unable to refresh user list: " + (message == null || message.isBlank() ? "unknown error" : message);
    }

    private String
    reason(String failureMessage)
    {
        return failureMessage == null || failureMessage.isBlank() ? NOT_DELETED : failureMessage;
    }
}

//////////////////////////////////////////////////////////////////////////////
