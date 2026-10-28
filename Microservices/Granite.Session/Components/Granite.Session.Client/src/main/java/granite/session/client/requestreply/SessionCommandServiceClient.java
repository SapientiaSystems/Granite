//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.client.requestreply;

import granite.session.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

public
class SessionCommandServiceClient
    extends RestEasyRestClient
    implements ISessionCommandServiceClient
{
    public
    SessionCommandServiceClient(ClientBuilder builder, String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "session-command-service/");
    }

    @Override
    public CompletionStage<LoginReply>
    login(LoginRequest request)
    {
        return
            doPostAsync(
                "login",
                LoginReply.class,
                request);
    }

    @Override
    public CompletionStage<StartOktaLoginReply>
    startOktaLogin(StartOktaLoginRequest request)
    {
        return
            doPostAsync(
                "start-okta-login",
                StartOktaLoginReply.class,
                request);
    }

    @Override
    public CompletionStage<CompleteOktaLoginReply>
    completeOktaLogin(CompleteOktaLoginRequest request)
    {
        return
            doPostAsync(
                "complete-okta-login",
                CompleteOktaLoginReply.class,
                request);
    }

    @Override
    public CompletionStage<LogoutReply>
    logout(LogoutRequest request)
    {
        return
            doPostAsync(
                "logout",
                LogoutReply.class,
                request);
    }

    @Override
    public CompletionStage<LogoutAllForUserReply>
    logoutAllForUser(LogoutAllForUserRequest request)
    {
        return
            doPostAsync(
                "logout-all-for-user",
                LogoutAllForUserReply.class,
                request);
    }

    @Override
    public CompletionStage<RefreshReply>
    refresh(RefreshRequest request)
    {
        return
            doPostAsync(
                "refresh",
                RefreshReply.class,
                request);
    }

    @Override
    public CompletionStage<ResolveSessionReply>
    resolveSession(ResolveSessionRequest request)
    {
        return
            doPostAsync(
                "resolve-session",
                ResolveSessionReply.class,
                request);
    }

    @Override
    public CompletionStage<IssueServiceTokenReply>
    issueServiceToken(IssueServiceTokenRequest request)
    {
        return
            doPostAsync(
                "issue-service-token",
                IssueServiceTokenReply.class,
                request);
    }

    @Override
    public SessionCommandServiceClient
    setAuthorizationHeader(String accessToken)
    {
        setHeader("Authorization","Bearer " + accessToken);
        return this;
    }

    @Override
    public void
    close() {}

    private static String
    preprocessBaseUrl(String baseUrl)
    {
        return
            baseUrl.endsWith("/")
                ? baseUrl
                : baseUrl + '/';
    }

}

//////////////////////////////////////////////////////////////////////////////
