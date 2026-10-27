//////////////////////////////////////////////////////////////////////////////
// InitializeUsersView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.foundation.core.security.ApplicationRoles;
import granite.foundation.core.security.RoleDefinition;
import granite.frontendcontroller.service.requestreply.RoleData;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

public
class InitializeUsersView
    implements IOutputAction<GetUsersOutputRequest,UsersOutput>
{
    private final IUserQueryServiceClient queryClient;
    private final ApplicationRoles        applicationRoles;

    public
    InitializeUsersView(IUserQueryServiceClient queryClient,ApplicationRoles applicationRoles)
    {
        this.queryClient      = queryClient;
        this.applicationRoles = applicationRoles;
    }

    @Override
    public CompletionStage<UsersOutput>
    apply(GetUsersOutputRequest request)
    {
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
            new UsersOutput()
                .setUsers(users)
                .setFailureMessages(failures)
                .setRoles(toRoleData())
                .setUserId(request.getUserId())
                .setSource(request.getSource())
                .setCommand(request.getCommand())
                .setDestination(request.getDestination());
    }

    private List<RoleData>
    toRoleData()
    {
        List<RoleData> roles = new ArrayList<>();

        for (RoleDefinition role : applicationRoles.getRoles())
        {
            List<String> implied = new ArrayList<>(role.getImpliedRoles());

            Collections.sort(implied);
            roles.add(new RoleData().setName(role.getName()).setLabel(role.getLabel()).setImpliedRoles(implied));
        }

        return roles;
    }

    private String
    refreshFailure(FindAllUsersReply reply)
    {
        String message = reply.getFailureMessage();

        return "Unable to refresh user list: " + (message == null || message.isBlank() ? "unknown error" : message);
    }
}

//////////////////////////////////////////////////////////////////////////////
