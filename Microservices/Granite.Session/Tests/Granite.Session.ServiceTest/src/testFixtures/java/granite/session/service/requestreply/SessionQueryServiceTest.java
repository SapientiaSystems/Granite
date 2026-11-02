//////////////////////////////////////////////////////////////////////////////
// SessionQueryServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import granite.user.service.requestreply.CredentialKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag("IntegrationStage")
public abstract
class SessionQueryServiceTest
{
    @Test
    public void
    testFindSessionsByKey()
    {
        LoginReply loginReply = login(uniqueUserName(),uniqueDeviceId());
        SessionData created = loginReply.getSessionData();

        FindSessionsByKeyReply reply =
            getSubject()
                .findSessionsByKey(
                    new FindSessionsByKeyRequest()
                        .setKey(SessionKey.ofSessionId(created.getSessionId()))
                        .setReturnIfFound(true))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getSessions());
        Assertions.assertTrue(containsSession(reply.getSessions(),created.getSessionId()));
    }

    @Test
    public void
    testFindSessionsByKeySet()
    {
        SessionData created1 = login(uniqueUserName(),uniqueDeviceId()).getSessionData();
        SessionData created2 = login(uniqueUserName(),uniqueDeviceId()).getSessionData();

        Set<Long> userIds = new HashSet<>();
        userIds.add(created1.getUserId());
        userIds.add(created2.getUserId());

        FindSessionsByKeySetReply reply =
            getSubject()
                .findSessionsByKeySet(
                    new FindSessionsByKeySetRequest()
                        .setKeySet(SessionKeySet.ofUserIds(userIds)))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getSessions());
        Assertions.assertTrue(containsSession(reply.getSessions(),created1.getSessionId()));
        Assertions.assertTrue(containsSession(reply.getSessions(),created2.getSessionId()));
    }

    @Test
    public void
    testFindSessionByUserIdAndDeviceId()
    {
        String deviceId = uniqueDeviceId();
        SessionData created = login(uniqueUserName(),deviceId).getSessionData();

        FindSessionByUserIdAndDeviceIdReply reply =
            getSubject()
                .findSessionByUserIdAndDeviceId(
                    new FindSessionByUserIdAndDeviceIdRequest()
                        .setUserId(created.getUserId())
                        .setDeviceId(deviceId))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getSession());
        Assertions.assertEquals(created.getSessionId(),reply.getSession().getSessionId());
        Assertions.assertEquals(deviceId,reply.getSession().getDeviceId());
    }

    @Test
    public void
    testFindSessionsOpenOnly()
    {
        SessionData open   = login(uniqueUserName(),uniqueDeviceId()).getSessionData();
        SessionData closed = login(uniqueUserName(),uniqueDeviceId()).getSessionData();

        logout(closed.getSessionId());

        FindSessionsReply reply = findSessions(false);

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertTrue(containsSession(reply.getSessions(),open.getSessionId()));
        Assertions.assertFalse(containsSession(reply.getSessions(),closed.getSessionId()));

        for (SessionData session : reply.getSessions())
            Assertions.assertNull(session.getClosedReason());
    }

    @Test
    public void
    testFindSessionsIncludeClosed()
    {
        SessionData open   = login(uniqueUserName(),uniqueDeviceId()).getSessionData();
        SessionData closed = login(uniqueUserName(),uniqueDeviceId()).getSessionData();

        logout(closed.getSessionId());

        FindSessionsReply reply = findSessions(true);

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertTrue(containsSession(reply.getSessions(),open.getSessionId()));
        Assertions.assertTrue(containsSession(reply.getSessions(),closed.getSessionId()));
        Assertions.assertEquals(
            SessionClosedReason.LOGOUT,
            findSession(reply.getSessions(),closed.getSessionId()).getClosedReason());
    }

    @Test
    public void
    testFindSessionsCarriesLastActivityAndLoginMethod()
    {
        SessionData created = login(uniqueUserName(),uniqueDeviceId()).getSessionData();
        SessionData found   = findSession(findSessions(false).getSessions(),created.getSessionId());

        Assertions.assertNotNull(found.getLastActivity());
        Assertions.assertEquals(SessionLoginMethod.PASSWORD,found.getLoginMethod());
    }

    protected abstract ISessionQueryService
    getSubject();

    protected abstract ISessionCommandService
    getCommandSubject();

    protected LoginReply
    login(String userName,String deviceId)
    {
        LoginReply reply =
            getCommandSubject()
                .login(
                    new LoginRequest()
                        .setUserName(userName)
                        .setCredential("password-" + userName)
                        .setCredentialKind(CredentialKind.PASSWORD)
                        .setDeviceId(deviceId))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getSessionData());
        Assertions.assertNotNull(reply.getAccessToken());
        return reply;
    }

    protected String
    uniqueUserName() { return "user-" + UUID.randomUUID() + "@example.com"; }

    protected String
    uniqueDeviceId() { return "device-" + UUID.randomUUID(); }

    protected void
    logout(Long sessionId)
    {
        LogoutReply reply =
            getCommandSubject()
                .logout(new LogoutRequest().setSessionId(sessionId))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
    }

    protected FindSessionsReply
    findSessions(boolean includeClosed)
    {
        FindSessionsReply reply =
            getSubject()
                .findSessions(new FindSessionsRequest().setIncludeClosed(includeClosed))
                .toCompletableFuture()
                .join();

        Assertions.assertNotNull(reply.getSessions());
        return reply;
    }

    private boolean
    containsSession(List<SessionData> sessions,Long sessionId)
    {
        return findSession(sessions,sessionId) != null;
    }

    private SessionData
    findSession(List<SessionData> sessions,Long sessionId)
    {
        for (SessionData session : sessions)
        {
            if (sessionId.equals(session.getSessionId()))
            {
                return session;
            }
        }
        return null;
    }
}

//////////////////////////////////////////////////////////////////////////////
