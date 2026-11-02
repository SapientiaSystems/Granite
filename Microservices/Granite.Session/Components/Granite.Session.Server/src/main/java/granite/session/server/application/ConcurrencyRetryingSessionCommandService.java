//////////////////////////////////////////////////////////////////////////////
// ConcurrencyRetryingSessionCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.service.requestreply.CloseSessionsReply;
import granite.session.service.requestreply.CloseSessionsRequest;
import granite.session.service.requestreply.ISessionCommandService;
import granite.session.service.requestreply.IssueServiceTokenReply;
import granite.session.service.requestreply.IssueServiceTokenRequest;
import granite.session.service.requestreply.IssueDelegatedTokenReply;
import granite.session.service.requestreply.IssueDelegatedTokenRequest;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.session.service.requestreply.LogoutAllForUserReply;
import granite.session.service.requestreply.LogoutAllForUserRequest;
import granite.session.service.requestreply.LogoutReply;
import granite.session.service.requestreply.LogoutRequest;
import granite.session.service.requestreply.RefreshReply;
import granite.session.service.requestreply.RefreshRequest;
import granite.session.service.requestreply.ResolveSessionReply;
import granite.session.service.requestreply.ResolveSessionRequest;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

public
class ConcurrencyRetryingSessionCommandService
    implements ISessionCommandService
{
    private final ISessionCommandService delegate;
    private final ConflictDetector       detector;
    private final Logger                 logger;

    private static final String UNABLE_TO_LOG_IN = "Unable to log in";

    public
    ConcurrencyRetryingSessionCommandService(ISessionCommandService delegate)
    {
        this.delegate = delegate;
        this.detector = new ConflictDetector();
        this.logger   = LogManager.getLogger(ConcurrencyRetryingSessionCommandService.class);
    }

    @Override
    public CompletionStage<LoginReply>
    login(LoginRequest request)
    {
        return
            retryOnce(
                () -> delegate.login(request),
                e ->
                    new LoginReply(request)
                        .setSuccess(false)
                        .setFailureMessage(UNABLE_TO_LOG_IN),
                request != null ? request.getRequestId() : null);
    }

    @Override
    public CompletionStage<StartOktaLoginReply>
    startOktaLogin(StartOktaLoginRequest request)
    {
        return delegate.startOktaLogin(request);
    }

    @Override
    public CompletionStage<CompleteOktaLoginReply>
    completeOktaLogin(CompleteOktaLoginRequest request)
    {
        return
            retryOnce(
                () -> delegate.completeOktaLogin(request),
                e ->
                    new CompleteOktaLoginReply(request)
                        .setSuccess(false)
                        .setFailureMessage(UNABLE_TO_LOG_IN),
                request != null ? request.getRequestId() : null);
    }

    @Override
    public CompletionStage<LogoutReply>
    logout(LogoutRequest request)
    {
        return delegate.logout(request);
    }

    @Override
    public CompletionStage<LogoutAllForUserReply>
    logoutAllForUser(LogoutAllForUserRequest request)
    {
        return delegate.logoutAllForUser(request);
    }

    @Override
    public CompletionStage<CloseSessionsReply>
    closeSessions(CloseSessionsRequest request)
    {
        return delegate.closeSessions(request);
    }

    @Override
    public CompletionStage<RefreshReply>
    refresh(RefreshRequest request)
    {
        return delegate.refresh(request);
    }

    @Override
    public CompletionStage<ResolveSessionReply>
    resolveSession(ResolveSessionRequest request)
    {
        return delegate.resolveSession(request);
    }

    @Override
    public CompletionStage<IssueServiceTokenReply>
    issueServiceToken(IssueServiceTokenRequest request)
    {
        return delegate.issueServiceToken(request);
    }

    @Override
    public CompletionStage<IssueDelegatedTokenReply>
    issueDelegatedToken(IssueDelegatedTokenRequest request)
    {
        return delegate.issueDelegatedToken(request);
    }

    private <R> CompletionStage<R>
    retryOnce(Supplier<CompletionStage<R>> attempt,Function<Throwable,R> failure,Object requestId)
    {
        return
            attempt(attempt)
                .exceptionallyCompose(
                    e ->
                    {
                        if (!detector.isConflict(e))
                            return CompletableFuture.failedFuture(e);

                        logger.info("concurrent login conflict; retrying once ({})",requestId);
                        return attempt(attempt);
                    })
                .exceptionally(
                    e ->
                    {
                        logger.warn("login failed ({}): {}",requestId,e.getClass().getName());
                        return failure.apply(e);
                    });
    }

    private <R> CompletableFuture<R>
    attempt(Supplier<CompletionStage<R>> attempt)
    {
        try
        {
            return attempt.get().toCompletableFuture();
        }
        catch (RuntimeException e)
        {
            return CompletableFuture.failedFuture(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
