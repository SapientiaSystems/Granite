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
                        .setKeys(UserKeySet.ofIds(getIds()))
                        .setReturnIfFound(true))
                .thenApply(this::processReply)
                .thenApply(this::toUsersOutput);
    }

    protected Set<Long>
    getIds()
    {
        return Set.of(1L,2L,3L,4L,5L,6L,7L,8L,9L,10L);
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
