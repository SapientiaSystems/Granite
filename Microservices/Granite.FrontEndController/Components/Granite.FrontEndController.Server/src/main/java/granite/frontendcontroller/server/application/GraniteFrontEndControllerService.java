//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.control.preference.InitializePreferencesView;
import granite.frontendcontroller.server.control.preference.SavePreferencesView;
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
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputReply;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.GetUsersOutputReply;
import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;
import granite.frontendcontroller.service.requestreply.HandlePreferencesInputReply;
import granite.frontendcontroller.service.requestreply.HandlePreferencesInputRequest;
import granite.frontendcontroller.service.requestreply.HandleSessionInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionInputRequest;
import granite.frontendcontroller.service.requestreply.HandleUsersInputReply;
import granite.frontendcontroller.service.requestreply.HandleUsersInputRequest;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import granite.frontendcontroller.service.requestreply.PreferencesInput;
import granite.frontendcontroller.service.requestreply.PreferencesOutput;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import strata.frontendcontroller.server.application.FrontEndControllerService;
import strata.frontendcontroller.server.application.GetOutputContext;
import strata.frontendcontroller.server.application.HandleInputContext;
import strata.frontendcontroller.service.requestreply.FrontEndInput;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class GraniteFrontEndControllerService
    extends    FrontEndControllerService
    implements IGraniteFrontEndControllerService
{
    private final GraniteFrontEndControllerContext                                 context;
    private final Map<String,IInputAction<UsersInput,UsersOutput>>                 usersInputActions;
    private final Map<String,IOutputAction<GetUsersOutputRequest,UsersOutput>>     usersOutputActions;
    private final Map<String,IInputAction<SessionInput,SessionOutput>>             sessionInputActions;
    private final Map<String,IOutputAction<GetSessionOutputRequest,SessionOutput>> sessionOutputActions;
    private final Map<String,IInputAction<PreferencesInput,PreferencesOutput>>             preferencesInputActions;
    private final Map<String,IOutputAction<GetPreferencesOutputRequest,PreferencesOutput>> preferencesOutputActions;

    public
    GraniteFrontEndControllerService(GraniteFrontEndControllerContext context)
    {
        super(context.getMapper(),context.getExecutor());
        this.context = context;

        IUserQueryServiceClient      userQueryClient      = context.getUserQueryClient();
        IUserCommandServiceClient    userCommandClient    = context.getUserCommandClient();
        ISessionCommandServiceClient sessionCommandClient = context.getSessionCommandClient();
        ISessionQueryServiceClient   sessionQueryClient   = context.getSessionQueryClient();
        SessionRequestContext        requestContext       = context.getSessionRequestContext();
        SessionLoginSettings         settings             = context.getSessionLoginSettings();

        usersInputActions =
            Map.of(
                "User.All->Save->UsersEditableView",new UpdateUsersView(userQueryClient,userCommandClient,context.getApplicationRoles()),
                "User.All->Delete->UsersEditableView",new DeleteUsersView(userQueryClient,userCommandClient,context.getApplicationRoles()));
        usersOutputActions =
            Map.of(
                "User.All->UsersEditableView",new InitializeUsersView(userQueryClient,context.getApplicationRoles()));
        sessionInputActions =
            Map.of(
                "Session.New->Login->LoginView",new LoginView(sessionCommandClient,requestContext,settings),
                "Session.New->StartOktaLogin->LoginView",new StartOktaLoginView(sessionCommandClient,requestContext,settings),
                "Session.New->CompleteOktaLogin->LoginView",new CompleteOktaLoginView(sessionCommandClient,requestContext,settings),
                "Session.Current->Logout->LoginView",new LogoutView(sessionCommandClient,requestContext));
        sessionOutputActions =
            Map.of(
                "Session.New->LoginView",new LoginMethodsView(settings),
                "Session.Current->SessionView",new SessionStatusView(sessionQueryClient,userQueryClient,requestContext));
        preferencesInputActions =
            Map.of(
                "Preferences.Current->Save->PreferencesView",new SavePreferencesView(userQueryClient,userCommandClient,requestContext,context.getPreferenceDefinitions()));
        preferencesOutputActions =
            Map.of(
                "Preferences.Current->PreferencesView",new InitializePreferencesView(userQueryClient,requestContext,context.getPreferenceDefinitions()));
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

    @Override
    public CompletionStage<HandlePreferencesInputReply>
    handlePreferencesInput(HandlePreferencesInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(preferencesInputActions,context),HandlePreferencesInputReply::new);
    }

    @Override
    public CompletionStage<GetPreferencesOutputReply>
    getPreferencesOutput(GetPreferencesOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(preferencesOutputActions,context),GetPreferencesOutputReply::new);
    }

    protected GraniteFrontEndControllerContext
    getContext() { return context; }

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
