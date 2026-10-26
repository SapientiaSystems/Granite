//////////////////////////////////////////////////////////////////////////////
// SessionFrontEndControllerService.java
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
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleSessionInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionInputRequest;
import granite.frontendcontroller.service.requestreply.ISessionFrontEndControllerService;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
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
class SessionFrontEndControllerService
    extends    FrontEndControllerService
    implements ISessionFrontEndControllerService
{
    private final Map<String,IInputAction<SessionInput,SessionOutput>>             inputActions;
    private final Map<String,IOutputAction<GetSessionOutputRequest,SessionOutput>> outputActions;

    public
    SessionFrontEndControllerService(
        ModelMapper                  mapper,
        Executor                     executor,
        ISessionCommandServiceClient commandClient,
        ISessionQueryServiceClient   queryClient,
        IUserQueryServiceClient      userQueryClient,
        SessionRequestContext        context,
        SessionLoginSettings         settings)
    {
        super(mapper,executor);
        inputActions =
            Map.of(
                "Session.New->Login->LoginView",new LoginView(commandClient,context,settings),
                "Session.New->StartOktaLogin->LoginView",new StartOktaLoginView(commandClient,context,settings),
                "Session.New->CompleteOktaLogin->LoginView",new CompleteOktaLoginView(commandClient,context,settings),
                "Session.Current->Logout->LoginView",new LogoutView(commandClient,context));
        outputActions =
            Map.of(
                "Session.New->LoginView",new LoginMethodsView(settings),
                "Session.Current->SessionView",new SessionStatusView(queryClient,userQueryClient,context));
    }

    @Override
    public CompletionStage<HandleSessionInputReply>
    handleSessionInput(HandleSessionInputRequest request)
    {
        return handleInput(request,this::dispatchInput,HandleSessionInputReply::new);
    }

    @Override
    public CompletionStage<GetSessionOutputReply>
    getSessionOutput(GetSessionOutputRequest request)
    {
        return getOutput(request,this::dispatchOutput,GetSessionOutputReply::new);
    }

    protected CompletionStage<HandleInputContext<SessionInput,SessionOutput>>
    dispatchInput(HandleInputContext<SessionInput,SessionOutput> context)
    {
        IInputAction<SessionInput,SessionOutput> action = inputActions.get(context.getKey());

        if (action == null)
            return CompletableFuture.failedFuture(noAction(context.getKey()));

        return action.apply(context.getInput()).thenApply(context::setOutput);
    }

    protected CompletionStage<GetOutputContext<GetSessionOutputRequest,SessionOutput>>
    dispatchOutput(GetOutputContext<GetSessionOutputRequest,SessionOutput> context)
    {
        IOutputAction<GetSessionOutputRequest,SessionOutput> action = outputActions.get(context.getKey());

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
