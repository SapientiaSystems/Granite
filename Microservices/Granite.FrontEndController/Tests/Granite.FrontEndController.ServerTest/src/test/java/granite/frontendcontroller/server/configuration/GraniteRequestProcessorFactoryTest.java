//////////////////////////////////////////////////////////////////////////////
// GraniteRequestProcessorFactoryTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.server.platform.SessionRequestContextTest;
import granite.frontendcontroller.service.requestreply.LoginInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.service.requestreply.FindSessionsByKeyReply;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.SessionData;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.UserData;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import strata.foundation.core.configuration.IConfiguration;
import strata.frontendcontroller.server.application.IRequestProcessor;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import javax.net.ssl.SSLContext;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteRequestProcessorFactoryTest
{
    private FakeSessionBackend              backend;
    private GraniteRequestProcessorFactory  factory;
    private MockHttpServletRequest          inbound;
    private String                          bearer;

    @BeforeEach
    public void
    setUp()
        throws Exception
    {
        backend = new FakeSessionBackend();

        IConfiguration configuration = Mockito.mock(IConfiguration.class);
        SslBundles     sslBundles    = Mockito.mock(SslBundles.class);
        SslBundle      bundle        = Mockito.mock(SslBundle.class);

        Mockito.when(configuration.getProperty(Mockito.anyString(),Mockito.anyString())).thenAnswer(i -> i.getArgument(1));
        for (String key : new String[] {"user-query-service.base-url","user-command-service.base-url","session-command-service.base-url","session-query-service.base-url"})
            Mockito.when(configuration.getProperty(key)).thenReturn(backend.baseUrl());
        Mockito.when(sslBundles.getBundle("granite-services")).thenReturn(bundle);
        Mockito.when(bundle.createSslContext()).thenReturn(SSLContext.getDefault());

        factory = new GraniteRequestProcessorFactory(configuration,sslBundles);

        bearer = SessionRequestContextTest.bearer("{\"jti\":\"11\",\"sub\":\"7\"}");
        inbound = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-input");
        inbound.setCookies(new Cookie("GRANITE_DID","device-1"));
        inbound.addHeader("Authorization",bearer);
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
        backend.stop();
    }

    @Test
    public void
    clientsWorkOffTheRequestThread()
        throws Exception
    {
        backend
            .respond(
                "login",
                new LoginReply()
                    .setSessionData(new SessionData().setSessionId(11L).setUserId(7L).setExpires(Instant.now().plusSeconds(600)).setAuthorities(Set.of("ROLE_ADMIN")))
                    .setSessionHandle("the-handle")
                    .setSuccess(true))
            .respond(
                "find-sessions-by-key",
                new FindSessionsByKeyReply()
                    .setSessions(List.of(new SessionData().setSessionId(11L).setUserId(7L).setExpires(Instant.now().plusSeconds(600)).setAuthorities(Set.of("ROLE_ADMIN"))))
                    .setSuccess(true))
            .respond(
                "find-users-by-key",
                new FindUsersByKeyReply().setFoundUsers(List.of(new UserData().setUserId(7L).setUserName("amy"))).setSuccess(true));

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inbound));

        IRequestProcessor processor = factory.create();

        RequestContextHolder.resetRequestAttributes();

        SessionOutput login =
            assertInstanceOf(
                SessionOutput.class,
                CompletableFuture
                    .supplyAsync(
                        () ->
                            processor
                                .process(
                                    new HandleInputRequest()
                                        .setInput(
                                            new LoginInput()
                                                .setUserName("amy")
                                                .setPassword("pw")
                                                .setSource("Session.New")
                                                .setCommand("Login")
                                                .setDestination("LoginView")))
                                .toCompletableFuture()
                                .join())
                    .join());

        SessionOutput status =
            assertInstanceOf(
                SessionOutput.class,
                CompletableFuture
                    .supplyAsync(
                        () ->
                            processor
                                .process(new GetOutputRequest().setUserId(7L).setSource("Session.Current").setDestination("SessionView"))
                                .toCompletableFuture()
                                .join())
                    .join());

        assertEquals("AUTHENTICATED",login.getStatus());
        assertEquals("AUTHENTICATED",status.getStatus());
        assertEquals("amy",status.getUserName());

        SessionRequestContext context = (SessionRequestContext)inbound.getAttribute(SessionRequestContext.ATTRIBUTE);

        assertTrue(context.getSetCookies().get(0).startsWith("GRANITE_SID=the-handle;"));
        assertTrue(backend.paths().get(0).endsWith("/session-command-service/login"));
        assertTrue(backend.bodies().get(0).contains("device-1"));
        assertTrue(backend.paths().get(1).endsWith("/session-query-service/find-sessions-by-key"));
        assertTrue(backend.paths().get(2).endsWith("/user-query-service/find-users-by-key"));
        assertEquals(List.of(bearer,bearer,bearer),backend.authorizations());
    }
}

//////////////////////////////////////////////////////////////////////////////
