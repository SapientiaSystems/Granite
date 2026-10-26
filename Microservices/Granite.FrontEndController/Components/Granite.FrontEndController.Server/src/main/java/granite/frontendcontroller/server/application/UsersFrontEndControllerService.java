//////////////////////////////////////////////////////////////////////////////
// UsersFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.control.user.DeleteUsersView;
import granite.frontendcontroller.server.control.user.InitializeUsersView;
import granite.frontendcontroller.server.control.user.UpdateUsersView;
import granite.frontendcontroller.service.requestreply.GetUsersOutputReply;
import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleUsersInputReply;
import granite.frontendcontroller.service.requestreply.HandleUsersInputRequest;
import granite.frontendcontroller.service.requestreply.IUsersFrontEndControllerService;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.modelmapper.ModelMapper;
import strata.frontendcontroller.server.application.FrontEndControllerService;
import strata.frontendcontroller.server.application.GetOutputContext;
import strata.frontendcontroller.server.application.HandleInputContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public
class UsersFrontEndControllerService
    extends    FrontEndControllerService
    implements IUsersFrontEndControllerService
{
    private final Map<String,IInputAction<UsersInput,UsersOutput>>             inputActions;
    private final Map<String,IOutputAction<GetUsersOutputRequest,UsersOutput>> outputActions;

    public
    UsersFrontEndControllerService(
        ModelMapper               mapper,
        Executor                  executor,
        IUserQueryServiceClient   queryClient,
        IUserCommandServiceClient commandClient)
    {
        super(mapper,executor);
        inputActions =
            Map.of(
                "User.All->Save->UsersEditableView",new UpdateUsersView(queryClient,commandClient),
                "User.All->Delete->UsersEditableView",new DeleteUsersView(queryClient,commandClient));
        outputActions =
            Map.of(
                "User.All->UsersEditableView",new InitializeUsersView(queryClient));
    }

    @Override
    public CompletionStage<HandleUsersInputReply>
    handleUsersInput(HandleUsersInputRequest request)
    {
        return handleInput(request,this::dispatchInput,HandleUsersInputReply::new);
    }

    @Override
    public CompletionStage<GetUsersOutputReply>
    getUsersOutput(GetUsersOutputRequest request)
    {
        return getOutput(request,this::dispatchOutput,GetUsersOutputReply::new);
    }

    protected CompletionStage<HandleInputContext<UsersInput,UsersOutput>>
    dispatchInput(HandleInputContext<UsersInput,UsersOutput> context)
    {
        IInputAction<UsersInput,UsersOutput> action = inputActions.get(context.getKey());

        if (action == null)
            return CompletableFuture.failedFuture(noAction(context.getKey()));

        return action.apply(context.getInput()).thenApply(context::setOutput);
    }

    protected CompletionStage<GetOutputContext<GetUsersOutputRequest,UsersOutput>>
    dispatchOutput(GetOutputContext<GetUsersOutputRequest,UsersOutput> context)
    {
        IOutputAction<GetUsersOutputRequest,UsersOutput> action = outputActions.get(context.getKey());

        if (action == null)
            return CompletableFuture.failedFuture(noAction(context.getKey()));

        return action.apply(context.getRequest()).thenApply(context::setOutput);
    }

    private IllegalArgumentException
    noAction(String key)
    {
        return new IllegalArgumentException("No action found for key: " + key);
    }
}

//////////////////////////////////////////////////////////////////////////////
