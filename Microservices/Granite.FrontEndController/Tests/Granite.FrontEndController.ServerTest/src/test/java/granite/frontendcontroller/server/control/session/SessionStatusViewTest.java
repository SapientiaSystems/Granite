//////////////////////////////////////////////////////////////////////////////
// SessionStatusViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.FindSessionsByKeyReply;
import granite.session.service.requestreply.FindSessionsByKeyRequest;
import granite.session.service.requestreply.SessionClosedReason;
import granite.session.service.requestreply.SessionData;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionStatusViewTest
{
    private ISessionQueryServiceClient sessionClient;
    private IUserQueryServiceClient    userClient;

    @BeforeEach
    public void
    setUp()
    {
        sessionClient = Mockito.mock(ISessionQueryServiceClient.class);
        userClient = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(userClient.findUsersByKey(Mockito.any(FindUsersByKeyRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindUsersByKeyReply()
                        .setFoundUsers(List.of(new UserData().setUserId(7L).setUserName("amy")))
                        .setSuccess(true)));
    }

    private void
    sessionIs(SessionData data)
    {
        Mockito
            .when(sessionClient.findSessionsByKey(Mockito.any(FindSessionsByKeyRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindSessionsByKeyReply()
                        .setSessions(data == null ? List.of() : List.of(data))
                        .setSuccess(true)));
    }

    private SessionData
    open()
    {
        return
            new SessionData()
                .setSessionId(11L)
                .setUserId(7L)
                .setExpires(Instant.now().plusSeconds(600))
                .setAuthorities(Set.of("ROLE_ADMIN","ROLE_AUDITOR"));
    }

    private void
    userHasPassword(String password)
    {
        Mockito
            .when(userClient.findUsersByKey(Mockito.any(FindUsersByKeyRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindUsersByKeyReply()
                        .setFoundUsers(List.of(new UserData().setUserId(7L).setUserName("amy").setPassword(password)))
                        .setSuccess(true)));
    }

    private SessionOutput
    run(SessionRequestContext context,SessionLoginSettings settings)
        throws Exception
    {
        return
            new SessionStatusView(sessionClient,userClient,context,settings)
                .apply(new GetSessionOutputRequest().setUserId(7L).setSource("Session.Current").setDestination("SessionView"))
                .toCompletableFuture()
                .get();
    }

    private SessionOutput
    run(SessionRequestContext context)
        throws Exception
    {
        return
            new SessionStatusView(sessionClient,userClient,context)
                .apply(new GetSessionOutputRequest().setUserId(7L).setSource("Session.Current").setDestination("SessionView"))
                .toCompletableFuture()
                .get();
    }

    private void
    assertUnauthenticated(SessionOutput output)
    {
        assertEquals("UNAUTHENTICATED",output.getStatus());
        assertNull(output.getUserName());
        assertTrue(output.getAuthorities().isEmpty());
    }

    @Test
    public void
    reflectsTheOpenSession()
        throws Exception
    {
        sessionIs(open());

        SessionOutput output = run(SessionTestSupport.context("d",11L,7L));

        assertEquals("AUTHENTICATED",output.getStatus());
        assertEquals("amy",output.getUserName());
        assertEquals(Set.of("ROLE_ADMIN","ROLE_AUDITOR"),output.getAuthorities());
        assertEquals("Session.Current",output.getSource());
        assertEquals("SessionView",output.getDestination());

        ArgumentCaptor<FindSessionsByKeyRequest> captor = ArgumentCaptor.forClass(FindSessionsByKeyRequest.class);

        Mockito.verify(sessionClient).findSessionsByKey(captor.capture());
        assertEquals("11",captor.getValue().getKey().getValue());
    }

    @Test
    public void
    passwordSetFollowsTheUsersMaskedPassword()
        throws Exception
    {
        sessionIs(open());

        userHasPassword("********");
        assertTrue(run(SessionTestSupport.context("d",11L,7L)).isPasswordSet());

        userHasPassword(null);
        assertFalse(run(SessionTestSupport.context("d",11L,7L)).isPasswordSet());
    }

    @Test
    public void
    passwordEnabledComesFromTheSettings()
        throws Exception
    {
        sessionIs(open());
        assertTrue(run(SessionTestSupport.context("d",11L,7L),new SessionLoginSettings(true,false)).isPasswordEnabled());
        assertFalse(run(SessionTestSupport.context("d",11L,7L),new SessionLoginSettings(false,true)).isPasswordEnabled());
    }

    @Test
    public void
    closedExpiredMissingOrForeignSessionsAreUnauthenticated()
        throws Exception
    {
        sessionIs(open().setClosedReason(SessionClosedReason.LOGOUT));
        assertUnauthenticated(run(SessionTestSupport.context("d",11L,7L)));

        sessionIs(open().setExpires(Instant.now().minusSeconds(1)));
        assertUnauthenticated(run(SessionTestSupport.context("d",11L,7L)));

        sessionIs(null);
        assertUnauthenticated(run(SessionTestSupport.context("d",11L,7L)));

        sessionIs(open());
        assertUnauthenticated(run(SessionTestSupport.context("d",11L,8L)));
    }

    @Test
    public void
    noSessionIdIsUnauthenticatedWithoutCallingSession()
        throws Exception
    {
        assertUnauthenticated(run(SessionTestSupport.context("d",null,null)));
        Mockito.verifyNoInteractions(sessionClient);
    }

    @Test
    public void
    missingUserIsUnauthenticated()
        throws Exception
    {
        sessionIs(open());
        Mockito
            .when(userClient.findUsersByKey(Mockito.any(FindUsersByKeyRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(new FindUsersByKeyReply().setFoundUsers(List.of()).setSuccess(true)));

        assertUnauthenticated(run(SessionTestSupport.context("d",11L,7L)));
    }
}

//////////////////////////////////////////////////////////////////////////////
