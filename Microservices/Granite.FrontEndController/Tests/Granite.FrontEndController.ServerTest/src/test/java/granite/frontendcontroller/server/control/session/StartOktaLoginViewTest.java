//////////////////////////////////////////////////////////////////////////////
// StartOktaLoginViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import strata.foundation.core.mapper.ObjectMapperSupplier;


import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class StartOktaLoginViewTest
{
    private static final String TRANSACTION_ID = "transaction-secret-value";
    private static final String AUTHORIZE_URL  = "https://okta.example.com/oauth2/default/v1/authorize?state=s";

    private ISessionCommandServiceClient commandClient;

    @BeforeEach
    public void
    setUp()
    {
        commandClient = Mockito.mock(ISessionCommandServiceClient.class);
    }

    @Test
    public void
    startSetsTheLoginCookieAndRedirects()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        Mockito
            .when(commandClient.startOktaLogin(Mockito.any(StartOktaLoginRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new StartOktaLoginReply()
                        .setSuccess(true)
                        .setAuthorizeUrl(AUTHORIZE_URL)
                        .setTransactionId(TRANSACTION_ID)));

        SessionOutput output = run(context,true);

        ArgumentCaptor<StartOktaLoginRequest> captor = ArgumentCaptor.forClass(StartOktaLoginRequest.class);

        Mockito.verify(commandClient).startOktaLogin(captor.capture());
        assertEquals("device-1",captor.getValue().getDeviceId());
        assertEquals("/users",captor.getValue().getReturnTo());
        assertEquals(AUTHORIZE_URL,output.getRedirectUrl());
        assertEquals("Session.New",output.getSource());
        assertEquals(1,context.getSetCookies().size());

        String cookie = context.getSetCookies().get(0);

        assertTrue(cookie.startsWith("GRANITE_LOGIN=" + TRANSACTION_ID + ";"),cookie);
        assertTrue(cookie.contains("HttpOnly"),cookie);
        assertTrue(cookie.contains("Secure"),cookie);
        assertTrue(cookie.contains("SameSite=Lax"),cookie);
        assertTrue(cookie.contains("Path=/"),cookie);
        assertTrue(cookie.contains("Max-Age=300"),cookie);
        assertFalse(serialize(output).contains(TRANSACTION_ID));
    }

    @Test
    public void
    disabledOktaIsRefusedWithoutCallingSession()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        assertRefused(run(context,false),context);
        Mockito.verifyNoInteractions(commandClient);
    }

    @Test
    public void
    missingDeviceCookieIsRefused()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context(null,null,null);

        assertRefused(run(context,true),context);
        Mockito.verifyNoInteractions(commandClient);
    }

    @Test
    public void
    sessionFailureIsRefused()
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        Mockito
            .when(commandClient.startOktaLogin(Mockito.any(StartOktaLoginRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new StartOktaLoginReply().setSuccess(false)));

        assertRefused(run(context,true),context);

        Mockito
            .when(commandClient.startOktaLogin(Mockito.any(StartOktaLoginRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));

        assertRefused(run(context,true),context);
    }

    private SessionOutput
    run(SessionRequestContext context,boolean oktaEnabled)
        throws Exception
    {
        return
            new StartOktaLoginView(commandClient,context,new SessionLoginSettings(true,oktaEnabled))
                .apply(
                    new SessionInput()
                        .setReturnTo("/users")
                        .setUserId(0L)
                        .setSource("Session.New")
                        .setCommand("StartOktaLogin")
                        .setDestination("LoginView"))
                .toCompletableFuture()
                .get();
    }

    private void
    assertRefused(SessionOutput output,SessionRequestContext context)
    {
        assertEquals("UNAUTHENTICATED",output.getStatus());
        assertNull(output.getRedirectUrl());
        assertTrue(context.getSetCookies().isEmpty());
    }

    private String
    serialize(Object value)
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapperSupplier().get();

        return mapper.writeValueAsString(value);
    }
}

//////////////////////////////////////////////////////////////////////////////
