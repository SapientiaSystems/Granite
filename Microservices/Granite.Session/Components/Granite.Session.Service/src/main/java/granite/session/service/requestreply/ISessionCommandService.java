//////////////////////////////////////////////////////////////////////////////
// ISessionCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface ISessionCommandService
{
    CompletionStage<LoginReply>
    login(LoginRequest request);

    CompletionStage<StartOktaLoginReply>
    startOktaLogin(StartOktaLoginRequest request);

    CompletionStage<CompleteOktaLoginReply>
    completeOktaLogin(CompleteOktaLoginRequest request);

    CompletionStage<LogoutReply>
    logout(LogoutRequest request);

    CompletionStage<LogoutAllForUserReply>
    logoutAllForUser(LogoutAllForUserRequest request);

    CompletionStage<CloseSessionsReply>
    closeSessions(CloseSessionsRequest request);

    CompletionStage<RefreshReply>
    refresh(RefreshRequest request);

    CompletionStage<ResolveSessionReply>
    resolveSession(ResolveSessionRequest request);

    CompletionStage<IssueServiceTokenReply>
    issueServiceToken(IssueServiceTokenRequest request);
}

//////////////////////////////////////////////////////////////////////////////
