//////////////////////////////////////////////////////////////////////////////
// UpdateUsersView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.CreateUsersReply;
import granite.user.service.requestreply.CreateUsersRequest;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UpdateUserData;
import granite.user.service.requestreply.UpdateUsersReply;
import granite.user.service.requestreply.UpdateUsersRequest;
import granite.user.service.requestreply.UserData;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class UpdateUsersView
    implements IInputAction<UsersInput,UsersOutput>
{
    private static final String NOT_SAVED = "not saved";

    private static final List<String> RESERVED_PREFIXES = List.of("preferences.","granite.");

    private final IUserQueryServiceClient   queryClient;
    private final IUserCommandServiceClient commandClient;
    private final SessionRequestContext     context;
    private final ApplicationRolesManager   applicationRoles;

    public
    UpdateUsersView(
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

        return
            create(input.getUsersToCreate(),failures)
                .thenCompose(v -> update(input.getUsersToUpdate(),failures))
                .thenCompose(v -> queryClient.findAllUsers(new FindAllUsersRequest()))
                .thenApply(reply -> toUsersOutput(input,reply,failures));
    }

    protected CompletionStage<Void>
    create(List<CreateUserData> users,List<String> failures)
    {
        if (users == null || users.isEmpty())
            return CompletableFuture.completedFuture(null);

        for (CreateUserData user : users)
            if (user.getValues() != null)
                user.setValues(withoutReserved(user.getValues()));

        return
            commandClient
                .createUsers(new CreateUsersRequest().setUsersToCreate(users))
                .thenAccept(reply -> collectCreateFailures(reply,failures));
    }

    protected CompletionStage<Void>
    update(List<UpdateUserData> users,List<String> failures)
    {
        if (users == null || users.isEmpty())
            return CompletableFuture.completedFuture(null);

        for (UpdateUserData user : users)
            user.getValues().ifPresent(values -> user.setValues(withoutReserved(values)));

        return
            commandClient
                .updateUsers(new UpdateUsersRequest().setUsersToUpdate(users))
                .thenAccept(reply -> collectUpdateFailures(reply,failures));
    }

    protected Map<String,Object>
    withoutReserved(Map<String,Object> values)
    {
        Map<String,Object> kept = new LinkedHashMap<>();

        for (Map.Entry<String,Object> entry : values.entrySet())
            if (!isReserved(entry.getKey()))
                kept.put(entry.getKey(),entry.getValue());

        return kept;
    }

    private boolean
    isReserved(String key)
    {
        for (String prefix : RESERVED_PREFIXES)
            if (key.startsWith(prefix))
                return true;

        return false;
    }

    protected void
    collectCreateFailures(CreateUsersReply reply,List<String> failures)
    {
        List<CreateUserData> failed = reply.getFailedUsers();

        if (failed != null && !failed.isEmpty())
        {
            for (CreateUserData user : failed)
                failures.add(user.getUserName() + ": " + reason(reply.getFailureMessage()));
        }
        else if (!reply.isSuccess())
            failures.add(reason(reply.getFailureMessage()));
    }

    protected void
    collectUpdateFailures(UpdateUsersReply reply,List<String> failures)
    {
        List<UpdateUserData> failed = reply.getFailedUsers();

        if (failed != null && !failed.isEmpty())
        {
            for (UpdateUserData user : failed)
                failures.add(
                    user.getUserName().orElse(String.valueOf(user.getUserId()))
                        + ": "
                        + reason(reply.getFailureMessage()));
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
        return failureMessage == null || failureMessage.isBlank() ? NOT_SAVED : failureMessage;
    }
}

//////////////////////////////////////////////////////////////////////////////
