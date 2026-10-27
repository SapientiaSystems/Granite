//////////////////////////////////////////////////////////////////////////////
// UpdateUsersView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.foundation.core.security.ApplicationRoles;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final ApplicationRoles          applicationRoles;

    public
    UpdateUsersView(
        IUserQueryServiceClient   queryClient,
        IUserCommandServiceClient commandClient,
        ApplicationRoles          applicationRoles)
    {
        this.queryClient      = queryClient;
        this.commandClient    = commandClient;
        this.applicationRoles = applicationRoles;
    }

    @Override
    public CompletionStage<UsersOutput>
    apply(UsersInput input)
    {
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

        if (!carriesValues(users))
            return sendUpdates(users,failures);

        return
            queryClient
                .findAllUsers(new FindAllUsersRequest())
                .thenCompose(
                    reply ->
                    {
                        if (!reply.isSuccess())
                        {
                            failures.add("Unable to verify reserved values: " + reason(reply.getFailureMessage()));
                            return CompletableFuture.<Void>completedFuture(null);
                        }

                        return sendUpdates(preserveReservedValues(users,reply.getUsers()),failures);
                    });
    }

    protected CompletionStage<Void>
    sendUpdates(List<UpdateUserData> users,List<String> failures)
    {
        return
            commandClient
                .updateUsers(new UpdateUsersRequest().setUsersToUpdate(users))
                .thenAccept(reply -> collectUpdateFailures(reply,failures));
    }

    protected boolean
    carriesValues(List<UpdateUserData> users)
    {
        for (UpdateUserData user : users)
            if (user.getValues().isPresent())
                return true;

        return false;
    }

    protected List<UpdateUserData>
    preserveReservedValues(List<UpdateUserData> users,List<UserData> storedUsers)
    {
        Map<Long,Map<String,Object>> storedById = new HashMap<>();

        if (storedUsers != null)
            for (UserData stored : storedUsers)
                storedById.put(stored.getUserId(),stored.getValues());

        List<UpdateUserData> result = new ArrayList<>();

        for (UpdateUserData user : users)
        {
            if (user.getValues().isEmpty())
            {
                result.add(user);
                continue;
            }

            Map<String,Object> values = new LinkedHashMap<>();

            for (Map.Entry<String,Object> entry : user.getValues().get().entrySet())
                if (!isReserved(entry.getKey()))
                    values.put(entry.getKey(),entry.getValue());

            Map<String,Object> stored = user.getUserId() == null ? null : storedById.get(user.getUserId());

            if (stored != null)
                for (Map.Entry<String,Object> entry : stored.entrySet())
                    if (isReserved(entry.getKey()))
                        values.put(entry.getKey(),entry.getValue());

            result.add(
                new UpdateUserData()
                    .setUserId(user.getUserId())
                    .setUserName(user.getUserName().orElse(null))
                    .setPassword(user.getPassword().orElse(null))
                    .setExternalIdentity(user.getExternalIdentity().orElse(null))
                    .setAuthorities(user.getAuthorities().orElse(null))
                    .setValues(values));
        }

        return result;
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
                .setUserId(input.getUserId())
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
