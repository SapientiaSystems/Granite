//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import granite.user.service.requestreply.CredentialKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@Tag("IntegrationStage")
public abstract
class SessionCommandServiceTest
{
    @Test
    public void
    testLogin()
    {
        LoginReply reply =
            getSubject()
                .login(newLoginRequest(uniqueUserName(),uniqueDeviceId()))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getAccessToken());
        Assertions.assertNotNull(reply.getSessionData());
        Assertions.assertNotNull(reply.getSessionData().getUserId());
        Assertions.assertNotNull(reply.getSessionData().getDeviceId());
        Assertions.assertNotNull(reply.getSessionData().getCreated());
        Assertions.assertNotNull(reply.getSessionData().getExpires());
        Assertions.assertNotNull(reply.getSessionData().getAuthorities());
        Assertions.assertFalse(reply.getSessionData().getAuthorities().isEmpty());
    }

    @Test
    public void
    testLoginWithOauth2()
    {
        LoginWithOauth2Reply reply =
            getSubject()
                .loginWithOauth2(
                    new LoginWithOauth2Request()
                        .setProvider(oauth2Provider())
                        .setIdToken(oauth2IdToken())
                        .setDeviceId(uniqueDeviceId()))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getAccessToken());
        Assertions.assertNotNull(reply.getSessionData());
        Assertions.assertNotNull(reply.getSessionData().getAuthorities());
    }

    @Test
    public void
    testLogout()
    {
        LoginReply loginReply =
            getSubject()
                .login(newLoginRequest(uniqueUserName(),uniqueDeviceId()))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(loginReply.isSuccess(),loginReply.getFailureMessage());
        Assertions.assertNotNull(loginReply.getSessionData());

        LogoutReply logoutReply =
            getSubject()
                .logout(
                    new LogoutRequest()
                        .setSessionId(loginReply.getSessionData().getSessionId()))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(logoutReply.isSuccess(),logoutReply.getFailureMessage());
    }

    @Test
    public void
    testLogoutAllForUser()
    {
        LoginReply loginReply =
            getSubject()
                .login(newLoginRequest(uniqueUserName(),uniqueDeviceId()))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(loginReply.isSuccess(),loginReply.getFailureMessage());
        Assertions.assertNotNull(loginReply.getSessionData());

        LogoutAllForUserReply logoutReply =
            getSubject()
                .logoutAllForUser(
                    new LogoutAllForUserRequest()
                        .setUserId(loginReply.getSessionData().getUserId()))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(logoutReply.isSuccess(),logoutReply.getFailureMessage());
    }

    @Test
    public void
    testRefresh()
    {
        LoginReply loginReply =
            getSubject()
                .login(newLoginRequest(uniqueUserName(),uniqueDeviceId()))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(loginReply.isSuccess(),loginReply.getFailureMessage());
        Assertions.assertNotNull(loginReply.getSessionData());

        RefreshReply refreshReply =
            getSubject()
                .refresh(
                    new RefreshRequest()
                        .setSessionId(loginReply.getSessionData().getSessionId()))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(refreshReply.isSuccess(),refreshReply.getFailureMessage());
        Assertions.assertNotNull(refreshReply.getAccessToken());
        Assertions.assertNotNull(refreshReply.getSessionData());
        Assertions.assertFalse(
            refreshReply.getSessionData().getExpires().isBefore(loginReply.getSessionData().getExpires()));
    }

    protected abstract ISessionCommandService
    getSubject();

    protected LoginRequest
    newLoginRequest(String userName,String deviceId)
    {
        return
            new LoginRequest()
                .setUserName(userName)
                .setCredential(credentialFor(userName))
                .setCredentialKind(CredentialKind.PASSWORD)
                .setDeviceId(deviceId);
    }

    protected String
    oauth2Provider() { return "test"; }

    protected String
    oauth2IdToken() { return "test-id-token-" + UUID.randomUUID(); }

    protected String
    credentialFor(String userName) { return "password-" + userName; }

    protected String
    uniqueUserName() { return "user-" + UUID.randomUUID() + "@example.com"; }

    protected String
    uniqueDeviceId() { return "device-" + UUID.randomUUID(); }
}

//////////////////////////////////////////////////////////////////////////////
