//////////////////////////////////////////////////////////////////////////////
// UpdateUsersView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

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
import strata.frontendcontroller.server.application.IHandleInputAction;
import strata.frontendcontroller.service.requestreply.FrontEndInputMapper;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class UpdateUsersView
    implements IHandleInputAction
{
    private static final String NOT_SAVED = "not saved";

    private final IUserQueryServiceClient   queryClient;
    private final IUserCommandServiceClient commandClient;
    private final FrontEndInputMapper       mapper;

    public
    UpdateUsersView(
        IUserQueryServiceClient   queryClient,
        IUserCommandServiceClient commandClient)
    {
        this.queryClient = queryClient;
        this.commandClient = commandClient;
        this.mapper = FrontEndInputMapper.of();
    }

    @Override
    public CompletionStage<FrontEndOutput>
    apply(HandleInputRequest request)
    {
        return
            mapper
                .map(UsersInput.class,request.getInput())
                .map(this::save)
                .orElseThrow();
    }

    protected CompletionStage<FrontEndOutput>
    save(UsersInput input)
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

        return
            commandClient
                .updateUsers(new UpdateUsersRequest().setUsersToUpdate(users))
                .thenAccept(reply -> collectUpdateFailures(reply,failures));
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

    protected FrontEndOutput
    toUsersOutput(UsersInput input,FindAllUsersReply reply,List<String> failures)
    {
        List<UserData> users = reply.isSuccess() ? reply.getUsers() : List.of();

        return
            new UsersOutput()
                .setUsers(users)
                .setFailureMessages(failures)
                .setUserId(input.getUserId())
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }

    private String
    reason(String failureMessage)
    {
        return failureMessage == null || failureMessage.isBlank() ? NOT_SAVED : failureMessage;
    }
}

//////////////////////////////////////////////////////////////////////////////
