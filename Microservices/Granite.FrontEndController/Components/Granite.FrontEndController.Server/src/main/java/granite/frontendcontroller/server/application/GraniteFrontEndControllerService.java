//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.control.client.ClientsLoader;
import granite.frontendcontroller.server.control.client.DeleteClientsView;
import granite.frontendcontroller.server.control.client.InitializeClientsView;
import granite.frontendcontroller.server.control.client.InitializeOwnedClientsView;
import granite.frontendcontroller.server.control.client.RotateClientSecret;
import granite.frontendcontroller.server.control.client.RotateOwnedClientSecret;
import granite.frontendcontroller.server.control.client.UpdateClientsView;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.control.preference.InitializePreferencesView;
import granite.frontendcontroller.server.control.preference.SavePreferencesView;
import granite.frontendcontroller.server.control.session.ChangePasswordView;
import granite.frontendcontroller.server.control.session.CompleteOktaLoginView;
import granite.frontendcontroller.server.control.session.LoginMethodsView;
import granite.frontendcontroller.server.control.session.LoginView;
import granite.frontendcontroller.server.control.session.LogoutView;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.control.session.SessionStatusView;
import granite.frontendcontroller.server.control.session.StartOktaLoginView;
import granite.frontendcontroller.server.control.feedback.FeedbackReviewLoader;
import granite.frontendcontroller.server.control.agent.InitializeAgentView;
import granite.frontendcontroller.server.control.agent.ResolveAgentActionView;
import granite.frontendcontroller.server.control.agent.SendAgentMessageView;
import granite.frontendcontroller.server.control.content.InitializeContentView;
import granite.frontendcontroller.server.control.content.OpenContentView;
import granite.frontendcontroller.server.control.content.SearchContentView;
import granite.frontendcontroller.server.control.survey.SurveyReviewLoader;
import granite.frontendcontroller.server.control.feedback.InitializeFeedbackReviewView;
import granite.frontendcontroller.server.control.survey.InitializeSurveyReviewView;
import granite.frontendcontroller.server.control.feedback.InitializeFeedbackView;
import granite.frontendcontroller.server.control.survey.InitializeSurveyView;
import granite.frontendcontroller.server.control.feedback.SaveFeedbackReviewView;
import granite.frontendcontroller.server.control.survey.SaveSurveyReviewView;
import granite.frontendcontroller.server.control.feedback.SubmitFeedbackView;
import granite.frontendcontroller.server.control.survey.SubmitSurveyView;
import granite.frontendcontroller.server.control.home.HomeMetricsCalculator;
import granite.frontendcontroller.server.control.home.HomeMetricsLoader;
import granite.frontendcontroller.server.control.home.InitializeHomeView;
import granite.frontendcontroller.server.control.sessions.CloseSessionsView;
import granite.frontendcontroller.server.control.sessions.InitializeSessionsView;
import granite.frontendcontroller.server.control.sessions.SessionsLoader;
import granite.frontendcontroller.server.control.user.DeleteUsersView;
import granite.frontendcontroller.server.control.user.InitializeUsersView;
import granite.frontendcontroller.server.control.user.UpdateUsersView;
import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ClientsInput;
import granite.frontendcontroller.service.requestreply.ClientsOutput;
import granite.frontendcontroller.service.requestreply.GetClientsOutputReply;
import granite.frontendcontroller.service.requestreply.GetClientsOutputRequest;
import granite.frontendcontroller.service.requestreply.GetOwnedClientsOutputReply;
import granite.frontendcontroller.service.requestreply.GetOwnedClientsOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleClientsInputReply;
import granite.frontendcontroller.service.requestreply.HandleClientsInputRequest;
import granite.frontendcontroller.service.requestreply.HandleOwnedClientSecretInputReply;
import granite.frontendcontroller.service.requestreply.HandleOwnedClientSecretInputRequest;
import granite.frontendcontroller.service.requestreply.OwnedClientSecretInput;
import granite.frontendcontroller.service.requestreply.GetFeedbackOutputReply;
import granite.frontendcontroller.service.requestreply.GetHomeOutputReply;
import granite.frontendcontroller.service.requestreply.GetHomeOutputRequest;
import granite.frontendcontroller.service.requestreply.HomeOutput;
import granite.frontendcontroller.service.requestreply.AgentInput;
import granite.frontendcontroller.service.requestreply.AgentOutput;
import granite.frontendcontroller.service.requestreply.GetAgentOutputReply;
import granite.frontendcontroller.service.requestreply.GetAgentOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleAgentInputReply;
import granite.frontendcontroller.service.requestreply.HandleAgentInputRequest;
import granite.frontendcontroller.service.requestreply.ContentInput;
import granite.frontendcontroller.service.requestreply.ContentOutput;
import granite.frontendcontroller.service.requestreply.GetContentOutputReply;
import granite.frontendcontroller.service.requestreply.GetContentOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleContentInputReply;
import granite.frontendcontroller.service.requestreply.HandleContentInputRequest;
import granite.frontendcontroller.service.requestreply.GetSurveyOutputReply;
import granite.frontendcontroller.service.requestreply.GetFeedbackOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSurveyOutputRequest;
import granite.frontendcontroller.service.requestreply.GetFeedbackReviewOutputReply;
import granite.frontendcontroller.service.requestreply.GetSurveyReviewOutputReply;
import granite.frontendcontroller.service.requestreply.GetFeedbackReviewOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSurveyReviewOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleFeedbackInputReply;
import granite.frontendcontroller.service.requestreply.HandleSurveyInputReply;
import granite.frontendcontroller.service.requestreply.HandleFeedbackInputRequest;
import granite.frontendcontroller.service.requestreply.HandleSurveyInputRequest;
import granite.frontendcontroller.service.requestreply.HandleFeedbackReviewInputReply;
import granite.frontendcontroller.service.requestreply.HandleSurveyReviewInputReply;
import granite.frontendcontroller.service.requestreply.HandleFeedbackReviewInputRequest;
import granite.frontendcontroller.service.requestreply.HandleSurveyReviewInputRequest;
import granite.frontendcontroller.service.requestreply.FeedbackInput;
import granite.frontendcontroller.service.requestreply.SurveyInput;
import granite.frontendcontroller.service.requestreply.FeedbackOutput;
import granite.frontendcontroller.service.requestreply.SurveyOutput;
import granite.frontendcontroller.service.requestreply.FeedbackReviewInput;
import granite.frontendcontroller.service.requestreply.SurveyReviewInput;
import granite.frontendcontroller.service.requestreply.FeedbackReviewOutput;
import granite.frontendcontroller.service.requestreply.SurveyReviewOutput;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputReply;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSessionsOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionsOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleSessionsInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionsInputRequest;
import granite.frontendcontroller.service.requestreply.SessionsInput;
import granite.frontendcontroller.service.requestreply.SessionsOutput;
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

import java.time.Clock;
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
    private final Map<String,IInputAction<FeedbackInput,FeedbackOutput>>           feedbackInputActions;
    private final Map<String,IInputAction<AgentInput,AgentOutput>>               agentInputActions;
    private final Map<String,IOutputAction<GetAgentOutputRequest,AgentOutput>>   agentOutputActions;
    private final Map<String,IInputAction<ContentInput,ContentOutput>>           contentInputActions;
    private final Map<String,IOutputAction<GetContentOutputRequest,ContentOutput>> contentOutputActions;
    private final Map<String,IInputAction<SurveyInput,SurveyOutput>>           surveyInputActions;
    private final Map<String,IOutputAction<GetFeedbackOutputRequest,FeedbackOutput>> feedbackOutputActions;
    private final Map<String,IOutputAction<GetSurveyOutputRequest,SurveyOutput>> surveyOutputActions;
    private final Map<String,IInputAction<FeedbackReviewInput,FeedbackReviewOutput>> feedbackReviewInputActions;
    private final Map<String,IInputAction<SurveyReviewInput,SurveyReviewOutput>> surveyReviewInputActions;
    private final Map<String,IOutputAction<GetFeedbackReviewOutputRequest,FeedbackReviewOutput>> feedbackReviewOutputActions;
    private final Map<String,IOutputAction<GetSurveyReviewOutputRequest,SurveyReviewOutput>> surveyReviewOutputActions;
    private final Map<String,IInputAction<SessionsInput,SessionsOutput>>           sessionsInputActions;
    private final Map<String,IOutputAction<GetSessionsOutputRequest,SessionsOutput>> sessionsOutputActions;
    private final Map<String,IOutputAction<GetHomeOutputRequest,HomeOutput>>         homeOutputActions;
    private final Map<String,IInputAction<SessionInput,SessionOutput>>             sessionInputActions;
    private final Map<String,IOutputAction<GetSessionOutputRequest,SessionOutput>> sessionOutputActions;
    private final Map<String,IInputAction<PreferencesInput,PreferencesOutput>>             preferencesInputActions;
    private final Map<String,IOutputAction<GetPreferencesOutputRequest,PreferencesOutput>> preferencesOutputActions;
    private final Map<String,IInputAction<ClientsInput,ClientsOutput>>                     clientsInputActions;
    private final Map<String,IOutputAction<GetClientsOutputRequest,ClientsOutput>>         clientsOutputActions;
    private final Map<String,IInputAction<OwnedClientSecretInput,ClientsOutput>>           ownedClientInputActions;
    private final Map<String,IOutputAction<GetOwnedClientsOutputRequest,ClientsOutput>>    ownedClientOutputActions;

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
        SessionCookies               cookies              = context.getSessionCookies();

        usersInputActions =
            Map.of(
                "User.All->Save->UsersEditableView",new UpdateUsersView(userQueryClient,userCommandClient,requestContext,context.getApplicationRoles()),
                "User.All->Delete->UsersEditableView",new DeleteUsersView(userQueryClient,userCommandClient,requestContext,context.getApplicationRoles()));
        usersOutputActions =
            Map.of(
                "User.All->UsersEditableView",new InitializeUsersView(userQueryClient,requestContext,context.getApplicationRoles()));

        FeedbackReviewLoader feedbackReviewLoader = new FeedbackReviewLoader(context.getHelpServiceClients(),userQueryClient);

        feedbackInputActions =
            Map.of(
                "Feedback.New->Submit->FeedbackView",new SubmitFeedbackView(context.getHelpServiceClients(),requestContext));
        feedbackOutputActions =
            Map.of(
                "Feedback.New->FeedbackView",new InitializeFeedbackView(requestContext));
        feedbackReviewInputActions =
            Map.of(
                "Feedback.All->Save->FeedbackReviewView",new SaveFeedbackReviewView(context.getHelpServiceClients(),feedbackReviewLoader,requestContext));
        feedbackReviewOutputActions =
            Map.of(
                "Feedback.All->FeedbackReviewView",new InitializeFeedbackReviewView(feedbackReviewLoader,requestContext));

        agentInputActions =
            Map.of(
                "Agent.Current->Send->AgentView",new SendAgentMessageView(context.getHelpServiceClients(),requestContext),
                "Agent.Current->Approve->AgentView",new ResolveAgentActionView(context.getHelpServiceClients(),requestContext,true),
                "Agent.Current->Decline->AgentView",new ResolveAgentActionView(context.getHelpServiceClients(),requestContext,false));
        agentOutputActions =
            Map.of(
                "Agent.Current->AgentView",new InitializeAgentView(context.getHelpServiceClients(),requestContext));

        contentInputActions =
            Map.of(
                "Content.All->Search->ContentView",new SearchContentView(context.getHelpServiceClients(),requestContext),
                "Content.All->Open->ContentView",new OpenContentView(context.getHelpServiceClients(),requestContext));
        contentOutputActions =
            Map.of(
                "Content.All->ContentView",new InitializeContentView(context.getHelpServiceClients(),requestContext));

        SurveyReviewLoader surveyReviewLoader = new SurveyReviewLoader(context.getHelpServiceClients(),userQueryClient);

        surveyInputActions =
            Map.of(
                "Survey.New->Submit->SurveyView",new SubmitSurveyView(context.getHelpServiceClients(),requestContext));
        surveyOutputActions =
            Map.of(
                "Survey.New->SurveyView",new InitializeSurveyView(requestContext));
        surveyReviewInputActions =
            Map.of(
                "Survey.All->Save->SurveyReviewView",new SaveSurveyReviewView(context.getHelpServiceClients(),surveyReviewLoader,requestContext));
        surveyReviewOutputActions =
            Map.of(
                "Survey.All->SurveyReviewView",new InitializeSurveyReviewView(surveyReviewLoader,requestContext));

        HomeMetricsLoader homeLoader =
            new HomeMetricsLoader(
                context.getHelpServiceClients(),
                userQueryClient,
                sessionQueryClient,
                new HomeMetricsCalculator(context.getApplicationRoles(),Clock.systemDefaultZone()));

        homeOutputActions =
            Map.of(
                "Home.Current->HomeView",new InitializeHomeView(homeLoader,requestContext));

        SessionsLoader sessionsLoader = new SessionsLoader(sessionQueryClient,userQueryClient);

        sessionsInputActions =
            Map.of(
                "Session.Open->Close->SessionsView",new CloseSessionsView(sessionCommandClient,sessionsLoader,requestContext,context.getApplicationRoles()),
                "Session.All->Close->SessionsView",new CloseSessionsView(sessionCommandClient,sessionsLoader,requestContext,context.getApplicationRoles()));
        sessionsOutputActions =
            Map.of(
                "Session.Open->SessionsView",new InitializeSessionsView(sessionsLoader,requestContext,context.getApplicationRoles()),
                "Session.All->SessionsView",new InitializeSessionsView(sessionsLoader,requestContext,context.getApplicationRoles()));
        sessionInputActions =
            Map.of(
                "Session.New->Login->LoginView",new LoginView(sessionCommandClient,requestContext,settings,cookies),
                "Session.New->StartOktaLogin->LoginView",new StartOktaLoginView(sessionCommandClient,requestContext,settings,cookies),
                "Session.New->CompleteOktaLogin->LoginView",new CompleteOktaLoginView(sessionCommandClient,requestContext,settings,cookies),
                "Session.Current->Logout->LoginView",new LogoutView(sessionCommandClient,requestContext,cookies),
                "Session.Current->ChangePassword->ChangePasswordView",new ChangePasswordView(sessionCommandClient,requestContext,settings));
        sessionOutputActions =
            Map.of(
                "Session.New->LoginView",new LoginMethodsView(settings),
                "Session.Current->SessionView",new SessionStatusView(sessionQueryClient,userQueryClient,requestContext,settings));
        preferencesInputActions =
            Map.of(
                "Preferences.Current->Save->PreferencesView",new SavePreferencesView(userQueryClient,userCommandClient,requestContext,context.getPreferenceDefinitions()));
        preferencesOutputActions =
            Map.of(
                "Preferences.Current->PreferencesView",new InitializePreferencesView(userQueryClient,requestContext,context.getPreferenceDefinitions()));

        ClientsLoader clientsLoader =
            new ClientsLoader(context.getClientServiceClients(),userQueryClient,context.getApplicationRoles());

        clientsInputActions =
            Map.of(
                "Client.All->Save->ClientsEditableView",new UpdateClientsView(context.getClientServiceClients(),clientsLoader,requestContext),
                "Client.All->Delete->ClientsEditableView",new DeleteClientsView(context.getClientServiceClients(),clientsLoader,requestContext),
                "Client.All->Rotate->ClientsEditableView",new RotateClientSecret(context.getClientServiceClients(),clientsLoader,requestContext));
        clientsOutputActions =
            Map.of(
                "Client.All->ClientsEditableView",new InitializeClientsView(clientsLoader,requestContext));
        ownedClientInputActions =
            Map.of(
                "Client.Owned->Rotate->OwnedClientsView",new RotateOwnedClientSecret(context.getClientServiceClients(),clientsLoader,requestContext));
        ownedClientOutputActions =
            Map.of(
                "Client.Owned->OwnedClientsView",new InitializeOwnedClientsView(clientsLoader,requestContext));
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
    public CompletionStage<HandleFeedbackInputReply>
    handleFeedbackInput(HandleFeedbackInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(feedbackInputActions,context),HandleFeedbackInputReply::new);
    }

    @Override
    public CompletionStage<GetFeedbackOutputReply>
    getFeedbackOutput(GetFeedbackOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(feedbackOutputActions,context),GetFeedbackOutputReply::new);
    }

    @Override
    public CompletionStage<HandleFeedbackReviewInputReply>
    handleFeedbackReviewInput(HandleFeedbackReviewInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(feedbackReviewInputActions,context),HandleFeedbackReviewInputReply::new);
    }

    @Override
    public CompletionStage<GetFeedbackReviewOutputReply>
    getFeedbackReviewOutput(GetFeedbackReviewOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(feedbackReviewOutputActions,context),GetFeedbackReviewOutputReply::new);
    }

    @Override
    public CompletionStage<HandleAgentInputReply>
    handleAgentInput(HandleAgentInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(agentInputActions,context),HandleAgentInputReply::new);
    }

    @Override
    public CompletionStage<GetAgentOutputReply>
    getAgentOutput(GetAgentOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(agentOutputActions,context),GetAgentOutputReply::new);
    }

    @Override
    public CompletionStage<HandleContentInputReply>
    handleContentInput(HandleContentInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(contentInputActions,context),HandleContentInputReply::new);
    }

    @Override
    public CompletionStage<GetContentOutputReply>
    getContentOutput(GetContentOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(contentOutputActions,context),GetContentOutputReply::new);
    }

    @Override
    public CompletionStage<HandleSurveyInputReply>
    handleSurveyInput(HandleSurveyInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(surveyInputActions,context),HandleSurveyInputReply::new);
    }

    @Override
    public CompletionStage<GetSurveyOutputReply>
    getSurveyOutput(GetSurveyOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(surveyOutputActions,context),GetSurveyOutputReply::new);
    }

    @Override
    public CompletionStage<HandleSurveyReviewInputReply>
    handleSurveyReviewInput(HandleSurveyReviewInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(surveyReviewInputActions,context),HandleSurveyReviewInputReply::new);
    }

    @Override
    public CompletionStage<GetSurveyReviewOutputReply>
    getSurveyReviewOutput(GetSurveyReviewOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(surveyReviewOutputActions,context),GetSurveyReviewOutputReply::new);
    }

    @Override
    public CompletionStage<GetHomeOutputReply>
    getHomeOutput(GetHomeOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(homeOutputActions,context),GetHomeOutputReply::new);
    }

    @Override
    public CompletionStage<HandleSessionsInputReply>
    handleSessionsInput(HandleSessionsInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(sessionsInputActions,context),HandleSessionsInputReply::new);
    }

    @Override
    public CompletionStage<GetSessionsOutputReply>
    getSessionsOutput(GetSessionsOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(sessionsOutputActions,context),GetSessionsOutputReply::new);
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

    @Override
    public CompletionStage<HandleClientsInputReply>
    handleClientsInput(HandleClientsInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(clientsInputActions,context),HandleClientsInputReply::new);
    }

    @Override
    public CompletionStage<GetClientsOutputReply>
    getClientsOutput(GetClientsOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(clientsOutputActions,context),GetClientsOutputReply::new);
    }

    @Override
    public CompletionStage<HandleOwnedClientSecretInputReply>
    handleOwnedClientSecretInput(HandleOwnedClientSecretInputRequest request)
    {
        return handleInput(request,context -> dispatchInput(ownedClientInputActions,context),HandleOwnedClientSecretInputReply::new);
    }

    @Override
    public CompletionStage<GetOwnedClientsOutputReply>
    getOwnedClientsOutput(GetOwnedClientsOutputRequest request)
    {
        return getOutput(request,context -> dispatchOutput(ownedClientOutputActions,context),GetOwnedClientsOutputReply::new);
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
