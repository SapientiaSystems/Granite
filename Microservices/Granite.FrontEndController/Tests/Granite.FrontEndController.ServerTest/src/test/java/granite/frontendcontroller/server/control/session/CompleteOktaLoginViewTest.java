//////////////////////////////////////////////////////////////////////////////
// CompleteOktaLoginViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.CompleteOktaLoginInput;
import granite.frontendcontroller.service.requestreply.FrontEndJacksonModule;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.session.service.requestreply.SessionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import strata.foundation.core.mapper.ObjectMapperSupplier;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class CompleteOktaLoginViewTest
{
    private static final String TRANSACTION_ID = "transaction-secret-value";
    private static final String CODE           = "okta-code-secret-value";
    private static final String HANDLE         = "session-handle-secret-value";

    private ISessionCommandServiceClient commandClient;

    @BeforeEach
    public void
    setUp()
    {
        commandClient = Mockito.mock(ISessionCommandServiceClient.class);
    }

    @Test
    public void
    completeSetsTheSessionCookieAndClearsTheLoginCookie()
        throws Exception
    {
        SessionRequestContext context = context("device-1",TRANSACTION_ID);

        Mockito
            .when(commandClient.completeOktaLogin(Mockito.any(CompleteOktaLoginRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new CompleteOktaLoginReply()
                        .setSuccess(true)
                        .setSessionHandle(HANDLE)
                        .setUserName("alice@example.com")
                        .setReturnTo("/users")
                        .setSessionData(
                            new SessionData()
                                .setSessionId(11L)
                                .setUserId(7L)
                                .setExpires(Instant.now().plusSeconds(3600))
                                .setAuthorities(Set.of("ROLE_ADMIN")))));

        SessionOutput output = assertInstanceOf(SessionOutput.class,run(context,true));

        ArgumentCaptor<CompleteOktaLoginRequest> captor = ArgumentCaptor.forClass(CompleteOktaLoginRequest.class);

        Mockito.verify(commandClient).completeOktaLogin(captor.capture());
        assertEquals(CODE,captor.getValue().getCode());
        assertEquals("state-1",captor.getValue().getState());
        assertEquals(TRANSACTION_ID,captor.getValue().getTransactionId());
        assertEquals("device-1",captor.getValue().getDeviceId());
        assertEquals("AUTHENTICATED",output.getStatus());
        assertEquals("alice@example.com",output.getUserName());
        assertEquals(Set.of("ROLE_ADMIN"),output.getAuthorities());
        assertEquals("/users",output.getReturnTo());

        List<String> cookies = context.getSetCookies();

        assertEquals(2,cookies.size());
        assertTrue(cookies.stream().anyMatch(c -> c.startsWith("GRANITE_SID=" + HANDLE + ";") && c.contains("SameSite=Strict")));
        assertLoginCleared(cookies);

        String json = serialize(output);

        assertFalse(json.contains(HANDLE));
        assertFalse(json.contains(TRANSACTION_ID));
        assertFalse(json.contains(CODE));
    }

    @Test
    public void
    failedCompletionClearsTheLoginCookieOnly()
        throws Exception
    {
        SessionRequestContext context = context("device-1",TRANSACTION_ID);

        Mockito
            .when(commandClient.completeOktaLogin(Mockito.any(CompleteOktaLoginRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new CompleteOktaLoginReply().setSuccess(false).setFailureMessage("invalid credentials")));

        assertRefused(run(context,true));
        assertEquals(1,context.getSetCookies().size());
        assertLoginCleared(context.getSetCookies());
    }

    @Test
    public void
    sessionErrorClearsTheLoginCookieOnly()
        throws Exception
    {
        SessionRequestContext context = context("device-1",TRANSACTION_ID);

        Mockito
            .when(commandClient.completeOktaLogin(Mockito.any(CompleteOktaLoginRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));

        assertRefused(run(context,true));
        assertEquals(1,context.getSetCookies().size());
        assertLoginCleared(context.getSetCookies());
    }

    @Test
    public void
    missingLoginCookieIsRefusedWithoutCallingSession()
        throws Exception
    {
        SessionRequestContext context = context("device-1",null);

        assertRefused(run(context,true));
        Mockito.verifyNoInteractions(commandClient);
        assertLoginCleared(context.getSetCookies());
    }

    @Test
    public void
    missingDeviceCookieIsRefusedWithoutCallingSession()
        throws Exception
    {
        SessionRequestContext context = context(null,TRANSACTION_ID);

        assertRefused(run(context,true));
        Mockito.verifyNoInteractions(commandClient);
        assertLoginCleared(context.getSetCookies());
    }

    @Test
    public void
    disabledOktaIsRefusedWithoutCallingSession()
        throws Exception
    {
        SessionRequestContext context = context("device-1",TRANSACTION_ID);

        assertRefused(run(context,false));
        Mockito.verifyNoInteractions(commandClient);
        assertLoginCleared(context.getSetCookies());
    }

    private FrontEndOutput
    run(SessionRequestContext context,boolean oktaEnabled)
        throws Exception
    {
        return
            new CompleteOktaLoginView(commandClient,context,new SessionLoginSettings(true,oktaEnabled))
                .apply(
                    new HandleInputRequest()
                        .setInput(
                            new CompleteOktaLoginInput()
                                .setCode(CODE)
                                .setState("state-1")
                                .setUserId(0L)
                                .setSource("Session.New")
                                .setCommand("CompleteOktaLogin")
                                .setDestination("LoginView")))
                .toCompletableFuture()
                .get();
    }

    private SessionRequestContext
    context(String deviceId,String transactionId)
    {
        Map<String,String> cookies = new HashMap<>();

        if (deviceId != null)
            cookies.put("GRANITE_DID",deviceId);

        if (transactionId != null)
            cookies.put("GRANITE_LOGIN",transactionId);

        return new SessionRequestContext(cookies,null);
    }

    private void
    assertRefused(FrontEndOutput output)
    {
        SessionOutput session = assertInstanceOf(SessionOutput.class,output);

        assertEquals("UNAUTHENTICATED",session.getStatus());
        assertNull(session.getReturnTo());
        assertTrue(session.getAuthorities().isEmpty());
    }

    private void
    assertLoginCleared(List<String> cookies)
    {
        assertTrue(
            cookies.stream().anyMatch(c -> c.startsWith("GRANITE_LOGIN=;") && c.contains("Max-Age=0") && c.contains("SameSite=Lax")),
            String.valueOf(cookies));
        assertTrue(cookies.stream().noneMatch(c -> c.startsWith("GRANITE_SID=;")),String.valueOf(cookies));
    }

    private String
    serialize(Object value)
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapperSupplier().get().registerModule(new FrontEndJacksonModule());

        return mapper.writeValueAsString(value);
    }
}

//////////////////////////////////////////////////////////////////////////////
