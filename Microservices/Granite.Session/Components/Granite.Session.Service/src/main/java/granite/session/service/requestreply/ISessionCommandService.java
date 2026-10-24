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

    CompletionStage<LoginWithOauth2Reply>
    loginWithOauth2(LoginWithOauth2Request request);

    CompletionStage<LogoutReply>
    logout(LogoutRequest request);

    CompletionStage<LogoutAllForUserReply>
    logoutAllForUser(LogoutAllForUserRequest request);

    CompletionStage<RefreshReply>
    refresh(RefreshRequest request);

    CompletionStage<ResolveSessionReply>
    resolveSession(ResolveSessionRequest request);
}

//////////////////////////////////////////////////////////////////////////////
