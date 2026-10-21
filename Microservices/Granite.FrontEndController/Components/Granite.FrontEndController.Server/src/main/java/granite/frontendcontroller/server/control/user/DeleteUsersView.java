//////////////////////////////////////////////////////////////////////////////
// DeleteUsersView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.DestroyUsersReply;
import granite.user.service.requestreply.DestroyUsersRequest;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;
import strata.frontendcontroller.server.application.IHandleInputAction;
import strata.frontendcontroller.service.requestreply.FrontEndInputMapper;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class DeleteUsersView
    implements IHandleInputAction
{
    private static final String NOT_DELETED = "not deleted";

    private final IUserQueryServiceClient   queryClient;
    private final IUserCommandServiceClient commandClient;
    private final FrontEndInputMapper       mapper;

    public
    DeleteUsersView(
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
                .map(this::delete)
                .orElseThrow();
    }

    protected CompletionStage<FrontEndOutput>
    delete(UsersInput input)
    {
        List<String> failures = new ArrayList<>();
        Set<Long>    ids = new LinkedHashSet<>();

        for (Long id : input.getUserIdsToDelete())
        {
            if (Objects.equals(id,input.getUserId()))
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
        return failureMessage == null || failureMessage.isBlank() ? NOT_DELETED : failureMessage;
    }
}

//////////////////////////////////////////////////////////////////////////////
