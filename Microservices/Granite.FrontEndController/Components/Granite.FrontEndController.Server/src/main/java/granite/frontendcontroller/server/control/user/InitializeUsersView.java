//////////////////////////////////////////////////////////////////////////////
// InitializeUsersView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;
import strata.frontendcontroller.server.application.IGetOutputAction;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public
class InitializeUsersView
    implements IGetOutputAction
{
    private final IUserQueryServiceClient queryClient;

    public
    InitializeUsersView(IUserQueryServiceClient queryClient)
    {
        this.queryClient = queryClient;
    }

    @Override
    public CompletionStage<FrontEndOutput>
    apply(GetOutputRequest request)
    {
        return
            queryClient
                .findAllUsers(new FindAllUsersRequest())
                .thenApply(reply -> toUsersOutput(request,reply));
    }

    protected FrontEndOutput
    toUsersOutput(GetOutputRequest request,FindAllUsersReply reply)
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
                .setUserId(request.getUserId())
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
