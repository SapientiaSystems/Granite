//////////////////////////////////////////////////////////////////////////////
// InitializeUsersView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeUsersView
    implements IOutputAction<GetUsersOutputRequest,UsersOutput>
{
    private final IUserQueryServiceClient queryClient;
    private final SessionRequestContext   context;
    private final ApplicationRolesManager applicationRoles;

    public
    InitializeUsersView(
        IUserQueryServiceClient queryClient,
        SessionRequestContext   context,
        ApplicationRolesManager applicationRoles)
    {
        this.queryClient      = queryClient;
        this.context          = context;
        this.applicationRoles = applicationRoles;
    }

    @Override
    public CompletionStage<UsersOutput>
    apply(GetUsersOutputRequest request)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return
                CompletableFuture.completedFuture(
                    describe(request,null,UnauthenticatedUsersOutput.create(applicationRoles)));

        return
            queryClient
                .findAllUsers(new FindAllUsersRequest())
                .thenApply(reply -> toUsersOutput(request,reply));
    }

    protected UsersOutput
    toUsersOutput(GetUsersOutputRequest request,FindAllUsersReply reply)
    {
        List<UserData> users = List.of();
        List<String>   failures = new ArrayList<>();

        if (reply.isSuccess())
            users = reply.getUsers();
        else
            failures.add(refreshFailure(reply));

        return
            describe(
                request,
                context.getCallerUserId().orElse(null),
                new UsersOutput()
                    .setUsers(users)
                    .setFailureMessages(failures)
                    .setRoles(RoleCatalogData.from(applicationRoles)));
    }

    private UsersOutput
    describe(GetUsersOutputRequest request,Long userId,UsersOutput output)
    {
        return
            output
                .setUserId(userId)
                .setSource(request.getSource())
                .setCommand(request.getCommand())
                .setDestination(request.getDestination());
    }

    private String
    refreshFailure(FindAllUsersReply reply)
    {
        String message = reply.getFailureMessage();

        return "Unable to refresh user list: " + (message == null || message.isBlank() ? "unknown error" : message);
    }
}

//////////////////////////////////////////////////////////////////////////////
