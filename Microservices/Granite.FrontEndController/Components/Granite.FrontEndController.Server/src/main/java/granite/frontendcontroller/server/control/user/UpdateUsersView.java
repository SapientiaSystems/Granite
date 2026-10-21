//////////////////////////////////////////////////////////////////////////////
// InitializeHomeView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import jakarta.inject.Inject;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.*;
import strata.frontendcontroller.server.application.IHandleInputAction;
import strata.frontendcontroller.service.requestreply.FrontEndInputMapper;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public
class UpdateUsersView
    implements IHandleInputAction
{

    private final IUserQueryServiceClient   queryClient;
    private final IUserCommandServiceClient commandClient;
    private final FrontEndInputMapper                mapper;

    @Inject
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
                .map(
                    input ->
                        commandClient
                            .updateUsers(
                                new UpdateUsersRequest()
                                    .addHeader("Authorization",request.getHeaders().get("Authorization"))
                                    .setUsersToUpdate(toUpdateUsers(input.getUsersToUpdate())))
                            .thenApply(this::processReply)
                            .thenApply(this::toUsersOutput))
                .orElseThrow();
    }

    protected Set<String>
    getBuyerIds()
    {
        return Set.of("1","2","3","4","5","6","7","8","9","10");
    }

    protected List<UserData>
    processReply(UpdateUsersReply reply)
    {
        if (reply.isSuccess())
            return reply.getUpdatedUsers();

        return List.of();
    }

    protected List<UpdateUserData>
    toUpdateUsers(List<UpdateUserData> users)
    {
        return null;
    }

    protected UpdateUserData
    toUpdateUser(UserData user)
    {
        return
            new UpdateUserData();

    }
    protected FrontEndOutput
    toUsersOutput(List<UserData> users)
    {
        return
            new UsersOutput()
                .setUsers(users);

    }
}

//////////////////////////////////////////////////////////////////////////////
