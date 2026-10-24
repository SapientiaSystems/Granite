//////////////////////////////////////////////////////////////////////////////
// LoginSessionTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import strata.foundation.core.mapper.ObjectMapperSupplier;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.FrontEndJacksonModule;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.SessionData;
import granite.user.service.requestreply.CredentialKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static granite.frontendcontroller.server.control.session.SessionTestSupport.HANDLE;
import static granite.frontendcontroller.server.control.session.SessionTestSupport.PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class LoginSessionTest
{
    private ISessionCommandServiceClient commandClient;

    @BeforeEach
    public void
    setUp()
    {
        commandClient = Mockito.mock(ISessionCommandServiceClient.class);
    }

    private LoginReply
    goodReply()
    {
        return
            new LoginReply()
                .setSessionData(
                    new SessionData()
                        .setSessionId(11L)
                        .setUserId(7L)
                        .setExpires(Instant.now().plusSeconds(600))
                        .setAuthorities(Set.of("ROLE_ADMIN")))
                .setAccessToken("access-token-secret")
                .setRefreshToken("refresh-token-secret")
                .setSessionHandle(HANDLE);
    }

    private SessionOutput
    run(SessionRequestContext context,SessionLoginSettings settings,HandleInputRequest request)
        throws Exception
    {
        return
            assertInstanceOf(
                SessionOutput.class,
                new LoginView(commandClient,context,settings).apply(request).toCompletableFuture().get());
    }

    private void
    assertRefused(SessionOutput output,SessionRequestContext context)
    {
        assertEquals("UNAUTHENTICATED",output.getStatus());
        assertNull(output.getUserName());
        assertTrue(output.getAuthorities().isEmpty());
        assertTrue(context.getSetCookies().isEmpty());
    }

    @Test
    public void
    passwordLoginSetsTheSessionCookieAndReturnsNoToken()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        Mockito
            .when(commandClient.login(Mockito.any(LoginRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(goodReply().setSuccess(true)));

        SessionOutput output = run(context,new SessionLoginSettings(true,false),SessionTestSupport.login("amy",PASSWORD));

        assertEquals("AUTHENTICATED",output.getStatus());
        assertEquals("amy",output.getUserName());
        assertEquals(Set.of("ROLE_ADMIN"),output.getAuthorities());
        assertEquals("Session.New",output.getSource());
        assertEquals("Login",output.getCommand().orElseThrow());
        assertEquals("LoginView",output.getDestination());

        assertEquals(1,context.getSetCookies().size());

        String cookie = context.getSetCookies().get(0);

        assertTrue(cookie.startsWith("GRANITE_SID=" + HANDLE + ";"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("SameSite=Strict"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.matches(".*Max-Age=(59[0-9]|600).*"));

        String json = new ObjectMapperSupplier().get().registerModule(new FrontEndJacksonModule()).writeValueAsString(output);

        for (String secret : new String[] {HANDLE,"access-token-secret","refresh-token-secret",PASSWORD})
            assertFalse(json.contains(secret));

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);

        Mockito.verify(commandClient).login(captor.capture());
        assertEquals(CredentialKind.PASSWORD,captor.getValue().getCredentialKind());
        assertEquals("device-1",captor.getValue().getDeviceId());
        assertEquals("amy",captor.getValue().getUserName());
        assertEquals(PASSWORD,captor.getValue().getCredential());
    }

    @Test
    public void
    failedLoginIsGenericAndSetsNoCookie()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        Mockito
            .when(commandClient.login(Mockito.any(LoginRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new LoginReply().setSuccess(false).setFailureMessage("invalid credentials")));

        assertRefused(run(context,new SessionLoginSettings(true,false),SessionTestSupport.login("amy","bad")),context);
    }

    @Test
    public void
    successWithoutAHandleIsRefused()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        Mockito
            .when(commandClient.login(Mockito.any(LoginRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(goodReply().setSessionHandle(null).setSuccess(true)));

        assertRefused(run(context,new SessionLoginSettings(true,false),SessionTestSupport.login("amy",PASSWORD)),context);
    }

    @Test
    public void
    sessionFailureIsRefusedNotPropagated()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        Mockito
            .when(commandClient.login(Mockito.any(LoginRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));

        assertRefused(run(context,new SessionLoginSettings(true,false),SessionTestSupport.login("amy",PASSWORD)),context);
    }

    @Test
    public void
    disabledPasswordLoginIsRefusedWithoutCallingSession()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        assertRefused(run(context,new SessionLoginSettings(false,false),SessionTestSupport.login("amy",PASSWORD)),context);
        Mockito.verifyNoInteractions(commandClient);
    }

    @Test
    public void
    blankFieldsAreRefusedWithoutCallingSession()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);
        SessionLoginSettings  settings = new SessionLoginSettings(true,false);

        assertRefused(run(context,settings,SessionTestSupport.login(" ",PASSWORD)),context);
        assertRefused(run(context,settings,SessionTestSupport.login("amy","")),context);
        assertRefused(run(context,settings,SessionTestSupport.login(null,null)),context);
        Mockito.verifyNoInteractions(commandClient);
    }

    @Test
    public void
    missingDeviceCookieIsRefused()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context(null,null,null);

        assertRefused(run(context,new SessionLoginSettings(true,false),SessionTestSupport.login("amy",PASSWORD)),context);
        Mockito.verifyNoInteractions(commandClient);
    }
}

//////////////////////////////////////////////////////////////////////////////
