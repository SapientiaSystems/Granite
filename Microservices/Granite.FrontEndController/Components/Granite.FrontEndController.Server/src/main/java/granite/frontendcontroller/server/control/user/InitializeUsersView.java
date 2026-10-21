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
                .thenApply(reply -> toUsersOutput(request,processReply(reply)));
    }

    protected List<UserData>
    processReply(FindAllUsersReply reply)
    {
        if (reply.isSuccess())
            return reply.getUsers();

        return List.of();
    }

    protected FrontEndOutput
    toUsersOutput(GetOutputRequest request,List<UserData> users)
    {
        return
            new UsersOutput()
                .setUsers(users)
                .setUserId(request.getUserId())
                .setSource(request.getSource())
                .setCommand(request.getCommand())
                .setDestination(request.getDestination());
    }
}

//////////////////////////////////////////////////////////////////////////////
