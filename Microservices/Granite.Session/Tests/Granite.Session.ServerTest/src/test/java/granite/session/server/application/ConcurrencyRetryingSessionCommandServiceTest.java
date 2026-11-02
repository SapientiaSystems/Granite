//////////////////////////////////////////////////////////////////////////////
// ConcurrencyRetryingSessionCommandServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.service.requestreply.CloseSessionsReply;
import granite.session.service.requestreply.CloseSessionsRequest;
import granite.session.service.requestreply.ISessionCommandService;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import granite.session.service.requestreply.RefreshReply;
import granite.session.service.requestreply.RefreshRequest;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ConcurrencyRetryingSessionCommandServiceTest
{
    private ISessionCommandService                   delegate;
    private ConcurrencyRetryingSessionCommandService subject;
    private LoginRequest                             request;

    @BeforeEach
    public void
    setUp()
    {
        delegate = Mockito.mock(ISessionCommandService.class);
        subject  = new ConcurrencyRetryingSessionCommandService(delegate);
        request  = new LoginRequest().setUserName("alice@example.com");
    }

    @Test
    public void
    retriesOnceAfterCommitConflict()
    {
        LoginReply success = new LoginReply(request).setSuccess(true);

        Mockito
            .when(delegate.login(request))
            .thenThrow(new RollbackException("commit failed",new OptimisticLockException("stale")))
            .thenReturn(CompletableFuture.completedFuture(success));

        LoginReply reply = login();

        assertSame(success,reply);
        Mockito.verify(delegate,Mockito.times(2)).login(request);
    }

    @Test
    public void
    retriesOnceAfterExceptionallyCompletedConflict()
    {
        LoginReply success = new LoginReply(request).setSuccess(true);

        Mockito
            .when(delegate.login(request))
            .thenReturn(
                CompletableFuture.failedFuture(
                    new CompletionException(new ConcurrentLoginException("concurrent"))))
            .thenReturn(CompletableFuture.completedFuture(success));

        assertSame(success,login());
        Mockito.verify(delegate,Mockito.times(2)).login(request);
    }

    @Test
    public void
    doesNotRetryFailureReply()
    {
        LoginReply refused = new LoginReply(request).setSuccess(false).setFailureMessage("invalid credentials");

        Mockito
            .when(delegate.login(request))
            .thenReturn(CompletableFuture.completedFuture(refused));

        assertSame(refused,login());
        Mockito.verify(delegate,Mockito.times(1)).login(request);
    }

    @Test
    public void
    doesNotRetryNonConflictException()
    {
        Mockito
            .when(delegate.login(request))
            .thenThrow(new IllegalStateException("boom"));

        LoginReply reply = login();

        assertFalse(reply.isSuccess());
        assertEquals("Unable to log in",reply.getFailureMessage());
        Mockito.verify(delegate,Mockito.times(1)).login(request);
    }

    @Test
    public void
    secondConflictBecomesFailureReply()
    {
        Mockito
            .when(delegate.login(request))
            .thenThrow(new RollbackException("first"))
            .thenThrow(new RollbackException("second"));

        LoginReply reply = login();

        assertFalse(reply.isSuccess());
        assertEquals("Unable to log in",reply.getFailureMessage());
        assertNull(reply.getException());
        Mockito.verify(delegate,Mockito.times(2)).login(request);
    }

    @Test
    public void
    retriesCompleteOktaLoginOnce()
    {
        CompleteOktaLoginRequest okta    = new CompleteOktaLoginRequest().setTransactionId("t");
        CompleteOktaLoginReply   success = new CompleteOktaLoginReply(okta).setSuccess(true);

        Mockito
            .when(delegate.completeOktaLogin(okta))
            .thenThrow(new RollbackException("commit failed"))
            .thenReturn(CompletableFuture.completedFuture(success));

        CompleteOktaLoginReply reply = subject.completeOktaLogin(okta).toCompletableFuture().join();

        assertTrue(reply.isSuccess());
        Mockito.verify(delegate,Mockito.times(2)).completeOktaLogin(okta);
    }

    @Test
    public void
    secondCompleteOktaLoginConflictIsAGenericFailure()
    {
        CompleteOktaLoginRequest okta = new CompleteOktaLoginRequest().setTransactionId("t");

        Mockito
            .when(delegate.completeOktaLogin(okta))
            .thenThrow(new RollbackException("commit failed"));

        CompleteOktaLoginReply reply = subject.completeOktaLogin(okta).toCompletableFuture().join();

        assertFalse(reply.isSuccess());
        assertEquals("Unable to log in",reply.getFailureMessage());
        Mockito.verify(delegate,Mockito.times(2)).completeOktaLogin(okta);
    }

    @Test
    public void
    startOktaLoginPassesThrough()
    {
        StartOktaLoginRequest start = new StartOktaLoginRequest().setDeviceId("d");
        StartOktaLoginReply   reply = new StartOktaLoginReply(start).setSuccess(true);

        Mockito.when(delegate.startOktaLogin(start)).thenReturn(CompletableFuture.completedFuture(reply));

        assertTrue(subject.startOktaLogin(start).toCompletableFuture().join().isSuccess());
        Mockito.verify(delegate,Mockito.times(1)).startOktaLogin(start);
    }

    @Test
    public void
    otherOperationsPassThrough()
    {
        RefreshRequest refresh = new RefreshRequest().setRefreshToken("r");
        RefreshReply   reply   = new RefreshReply(refresh).setSuccess(true);

        Mockito
            .when(delegate.refresh(refresh))
            .thenReturn(CompletableFuture.completedFuture(reply));

        assertSame(reply,subject.refresh(refresh).toCompletableFuture().join());
        Mockito.verify(delegate,Mockito.times(1)).refresh(refresh);
    }

    @Test
    public void
    closeSessionsPassesThrough()
    {
        CloseSessionsRequest close = new CloseSessionsRequest().setSessionIds(Set.of(7L));
        CloseSessionsReply   reply = new CloseSessionsReply(close).setSuccess(true);

        Mockito
            .when(delegate.closeSessions(close))
            .thenReturn(CompletableFuture.completedFuture(reply));

        assertSame(reply,subject.closeSessions(close).toCompletableFuture().join());
        Mockito.verify(delegate,Mockito.times(1)).closeSessions(close);
    }

    private LoginReply
    login()
    {
        return subject.login(request).toCompletableFuture().join();
    }
}

//////////////////////////////////////////////////////////////////////////////
