//////////////////////////////////////////////////////////////////////////////
// InitializeHomeView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.user.service.requestreply.FindUsersByKeySetReply;
import granite.user.service.requestreply.FindUsersByKeySetRequest;
import jakarta.inject.Inject;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.UserData;
import granite.user.service.requestreply.UserKeySet;
import strata.frontendcontroller.server.application.IGetOutputAction;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public
class InitializeUsersView
    implements IGetOutputAction
{

    private final IUserQueryServiceClient userClient;

    @Inject
    public
    InitializeUsersView(IUserQueryServiceClient userClient)
    {
        this.userClient = userClient;
    }

    @Override
    public CompletionStage<FrontEndOutput>
    apply(GetOutputRequest request)
    {
        return
            userClient
                .findUsersByKeySet(
                    new FindUsersByKeySetRequest()
                        .addHeader("Authorization",request.getHeaders().get("Authorization"))
                        .setKeys(UserKeySet.ofFoos(getFoos()))
                        .setReturnIfFound(true))
                .thenApply(this::processReply)
                .thenApply(this::toUsersOutput);
    }

    protected Set<String>
    getFoos()
    {
        return Set.of("1","2","3","4","5","6","7","8","9","10");
    }

    protected List<UserData>
    processReply(FindUsersByKeySetReply reply)
    {
        if (reply.isSuccess())
            return reply.getFoundUsers();

        return List.of();
    }

    protected UsersOutput
    toUsersOutput(List<UserData> users)
    {
        return
            new UsersOutput()
                .setUsers(users);

    }
}

//////////////////////////////////////////////////////////////////////////////
