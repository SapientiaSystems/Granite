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
import granite.frontendcontroller.server.control.feedback.FeedbackReviewLoader;
import granite.frontendcontroller.server.control.survey.SurveyReviewLoader;
import granite.frontendcontroller.server.control.feedback.InitializeFeedbackReviewView;
import granite.frontendcontroller.server.control.survey.InitializeSurveyReviewView;
import granite.frontendcontroller.server.control.feedback.InitializeFeedbackView;
import granite.frontendcontroller.server.control.survey.InitializeSurveyView;
import granite.frontendcontroller.server.control.feedback.SaveFeedbackReviewView;
import granite.frontendcontroller.server.control.survey.SaveSurveyReviewView;
import granite.frontendcontroller.server.control.feedback.SubmitFeedbackView;
import granite.frontendcontroller.server.control.survey.SubmitSurveyView;
import granite.frontendcontroller.server.control.sessions.CloseSessionsView;
import granite.frontendcontroller.server.control.sessions.InitializeSessionsView;
import granite.frontendcontroller.server.control.sessions.SessionsLoader;
import granite.frontendcontroller.server.control.user.DeleteUsersView;
import granite.frontendcontroller.server.control.user.InitializeUsersView;
import granite.frontendcontroller.server.control.user.UpdateUsersView;
import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.GetFeedbackOutputReply;
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
    private final Map<String,IInputAction<SurveyInput,SurveyOutput>>           surveyInputActions;
    private final Map<String,IOutputAction<GetFeedbackOutputRequest,FeedbackOutput>> feedbackOutputActions;
    private final Map<String,IOutputAction<GetSurveyOutputRequest,SurveyOutput>> surveyOutputActions;
    private final Map<String,IInputAction<FeedbackReviewInput,FeedbackReviewOutput>> feedbackReviewInputActions;
    private final Map<String,IInputAction<SurveyReviewInput,SurveyReviewOutput>> surveyReviewInputActions;
    private final Map<String,IOutputAction<GetFeedbackReviewOutputRequest,FeedbackReviewOutput>> feedbackReviewOutputActions;
    private final Map<String,IOutputAction<GetSurveyReviewOutputRequest,SurveyReviewOutput>> surveyReviewOutputActions;
    private final Map<String,IInputAction<SessionsInput,SessionsOutput>>           sessionsInputActions;
    private final Map<String,IOutputAction<GetSessionsOutputRequest,SessionsOutput>> sessionsOutputActions;
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

        SessionsLoader sessionsLoader = new SessionsLoader(sessionQueryClient,userQueryClient);

        sessionsInputActions =
            Map.of(
                "Session.Open->Close->SessionsView",new CloseSessionsView(sessionCommandClient,sessionsLoader,requestContext),
                "Session.All->Close->SessionsView",new CloseSessionsView(sessionCommandClient,sessionsLoader,requestContext));
        sessionsOutputActions =
            Map.of(
                "Session.Open->SessionsView",new InitializeSessionsView(sessionsLoader,requestContext),
                "Session.All->SessionsView",new InitializeSessionsView(sessionsLoader,requestContext));
        sessionInputActions =
            Map.of(
                "Session.New->Login->LoginView",new LoginView(sessionCommandClient,requestContext,settings,cookies),
                "Session.New->StartOktaLogin->LoginView",new StartOktaLoginView(sessionCommandClient,requestContext,settings,cookies),
                "Session.New->CompleteOktaLogin->LoginView",new CompleteOktaLoginView(sessionCommandClient,requestContext,settings,cookies),
                "Session.Current->Logout->LoginView",new LogoutView(sessionCommandClient,requestContext,cookies));
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
