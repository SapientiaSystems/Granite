//////////////////////////////////////////////////////////////////////////////
// LogoutSessionTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.LogoutOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.LogoutReply;
import granite.session.service.requestreply.LogoutRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class LogoutSessionTest
{
    private ISessionCommandServiceClient commandClient;

    @BeforeEach
    public void
    setUp()
    {
        commandClient = Mockito.mock(ISessionCommandServiceClient.class);
    }

    private LogoutOutput
    run(SessionRequestContext context)
        throws Exception
    {
        return
            assertInstanceOf(
                LogoutOutput.class,
                new LogoutView(commandClient,context).apply(SessionTestSupport.logout()).toCompletableFuture().get());
    }

    @Test
    public void
    logoutClosesTheCallersSessionAndClearsTheCookie()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("d",11L,7L);

        Mockito
            .when(commandClient.logout(Mockito.any(LogoutRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new LogoutReply().setSuccess(true)));

        LogoutOutput output = run(context);

        assertTrue(output.isLoggedOut());
        assertNull(output.getEndSessionUrl());
        assertEquals("Session.Current",output.getSource());
        assertEquals("Logout",output.getCommand().orElseThrow());

        ArgumentCaptor<LogoutRequest> captor = ArgumentCaptor.forClass(LogoutRequest.class);

        Mockito.verify(commandClient).logout(captor.capture());
        assertEquals(11L,captor.getValue().getSessionId());
        assertEquals(1,context.getSetCookies().size());
        assertTrue(context.getSetCookies().get(0).startsWith("GRANITE_SID=;"));
        assertTrue(context.getSetCookies().get(0).contains("Max-Age=0"));
    }

    @Test
    public void
    oktaLogoutCarriesTheEndSessionUrl()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("d",11L,7L);
        String                url     = "https://okta.example.com/oauth2/default/v1/logout?id_token_hint=x";

        Mockito
            .when(commandClient.logout(Mockito.any(LogoutRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new LogoutReply().setSuccess(true).setEndSessionUrl(url)));

        LogoutOutput output = run(context);

        assertTrue(output.isLoggedOut());
        assertEquals(url,output.getEndSessionUrl());
        assertTrue(context.getSetCookies().get(0).contains("Max-Age=0"));
    }

    @Test
    public void
    noSessionIdClearsTheCookieWithoutCallingSession()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("d",null,null);

        assertTrue(run(context).isLoggedOut());
        assertEquals(1,context.getSetCookies().size());
        Mockito.verifyNoInteractions(commandClient);
    }

    @Test
    public void
    failedLogoutKeepsCookie()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("d",11L,7L);

        Mockito
            .when(commandClient.logout(Mockito.any(LogoutRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new LogoutReply().setSuccess(false)));

        assertFalse(run(context).isLoggedOut());
        assertTrue(context.getSetCookies().isEmpty());

        Mockito
            .when(commandClient.logout(Mockito.any(LogoutRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));

        assertFalse(run(context).isLoggedOut());
        assertTrue(context.getSetCookies().isEmpty());
    }
}

//////////////////////////////////////////////////////////////////////////////
