//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.control.session.CompleteOktaLoginView;
import granite.frontendcontroller.server.control.session.LoginMethodsView;
import granite.frontendcontroller.server.control.session.LoginView;
import granite.frontendcontroller.server.control.session.LogoutView;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.control.session.SessionStatusView;
import granite.frontendcontroller.server.control.session.StartOktaLoginView;
import granite.frontendcontroller.server.control.user.DeleteUsersView;
import granite.frontendcontroller.server.control.user.InitializeUsersView;
import granite.frontendcontroller.server.control.user.UpdateUsersView;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.GetUsersOutputReply;
import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleSessionInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionInputRequest;
import granite.frontendcontroller.service.requestreply.HandleUsersInputReply;
import granite.frontendcontroller.service.requestreply.HandleUsersInputRequest;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.modelmapper.ModelMapper;
import strata.frontendcontroller.server.application.FrontEndControllerService;
import strata.frontendcontroller.server.application.GetOutputContext;
import strata.frontendcontroller.server.application.HandleInputContext;
import strata.frontendcontroller.service.requestreply.FrontEndInput;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public
class GraniteFrontEndControllerService
    extends    FrontEndControllerService
    implements IGraniteFrontEndControllerService
{
    private final Map<String,IInputAction<UsersInput,UsersOutput>>                 usersInputActions;
    private final Map<String,IOutputAction<GetUsersOutputRequest,UsersOutput>>     usersOutputActions;
    private final Map<String,IInputAction<SessionInput,SessionOutput>>             sessionInputActions;
    private final Map<String,IOutputAction<GetSessionOutputRequest,SessionOutput>> sessionOutputActions;

    public
    GraniteFrontEndControllerService(
        ModelMapper                  mapper,
        Executor                     executor,
        IUserQueryServiceClient      userQueryClient,
        IUserCommandServiceClient    userCommandClient,
        ISessionCommandServiceClient sessionCommandClient,
        ISessionQueryServiceClient   sessionQueryClient,
        SessionRequestContext        context,
        SessionLoginSettings         settings)
    {
        super(mapper,executor);
        usersInputActions =
            Map.of(
                "User.All->Save->UsersEditableView",new UpdateUsersView(userQueryClient,userCommandClient),
                "User.All->Delete->UsersEditableView",new DeleteUsersView(userQueryClient,userCommandClient));
        usersOutputActions =
            Map.of(
                "User.All->UsersEditableView",new InitializeUsersView(userQueryClient));
        sessionInputActions =
            Map.of(
                "Session.New->Login->LoginView",new LoginView(sessionCommandClient,context,settings),
                "Session.New->StartOktaLogin->LoginView",new StartOktaLoginView(sessionCommandClient,context,settings),
                "Session.New->CompleteOktaLogin->LoginView",new CompleteOktaLoginView(sessionCommandClient,context,settings),
                "Session.Current->Logout->LoginView",new LogoutView(sessionCommandClient,context));
        sessionOutputActions =
            Map.of(
                "Session.New->LoginView",new LoginMethodsView(settings),
                "Session.Current->SessionView",new SessionStatusView(sessionQueryClient,userQueryClient,context));
    }

    @Override
    public CompletionStage<HandleUsersInputReply>
    handleUsersInput(HandleUsersInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(usersInputActions,context),HandleUsersInputReply::new);
    }

    @Override
    public CompletionStage<GetUsersOutputReply>
    getUsersOutput(GetUsersOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(usersOutputActions,context),GetUsersOutputReply::new);
    }

    @Override
    public CompletionStage<HandleSessionInputReply>
    handleSessionInput(HandleSessionInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(sessionInputActions,context),HandleSessionInputReply::new);
    }

    @Override
    public CompletionStage<GetSessionOutputReply>
    getSessionOutput(GetSessionOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(sessionOutputActions,context),GetSessionOutputReply::new);
    }

    protected <I extends FrontEndInput,O extends FrontEndOutput> CompletionStage<HandleInputContext<I,O>>
    dispatchInput(Map<String,IInputAction<I,O>> actions,HandleInputContext<I,O> context)
    {
        IInputAction<I,O> action = actions.get(context.getKey());

        if (action == null)
            return CompletableFuture.failedFuture(noAction(context.getKey()));

        return action.apply(context.getInput()).thenApply(context::setOutput);
    }

    protected <R extends GetOutputRequest,O extends FrontEndOutput> CompletionStage<GetOutputContext<R,O>>
    dispatchOutput(Map<String,IOutputAction<R,O>> actions,GetOutputContext<R,O> context)
    {
        IOutputAction<R,O> action = actions.get(context.getKey());

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
