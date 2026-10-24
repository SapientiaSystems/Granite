//////////////////////////////////////////////////////////////////////////////
// SessionHttpTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.frontendcontroller.server.platform.ResponseCookieAdvice;
import granite.frontendcontroller.server.platform.SessionRequestContextTest;
import granite.frontendcontroller.service.requestreply.FrontEndJacksonModule;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LogoutReply;
import granite.session.service.requestreply.SessionData;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.mapper.ObjectMapperSupplier;
import strata.frontendcontroller.server.application.FrontEndControllerService;
import strata.frontendcontroller.server.platform.UserFrontEndControllerServiceController;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;
import strata.frontendcontroller.service.requestreply.IFrontEndControllerService;

import javax.net.ssl.SSLContext;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Tag("CommitStage")
public
class SessionHttpTest
{
    private static final String PASSWORD = "very-secret-password";
    private static final String HANDLE   = "opaque-handle-value";

    private FakeSessionBackend backend;
    private MockMvc            mvc;

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

        GraniteRequestProcessorFactory factory = new GraniteRequestProcessorFactory(configuration,sslBundles);
        IFrontEndControllerService     service = Mockito.mock(IFrontEndControllerService.class);

        Mockito
            .when(service.handleInput(Mockito.any(HandleInputRequest.class)))
            .thenAnswer(
                i -> new FrontEndControllerService(factory.create(),new ModelMapper(),Runnable::run).handleInput(i.getArgument(0)));
        Mockito
            .when(service.getOutput(Mockito.any(GetOutputRequest.class)))
            .thenAnswer(
                i -> new FrontEndControllerService(factory.create(),new ModelMapper(),Runnable::run).getOutput(i.getArgument(0)));

        ObjectMapper mapper = new ObjectMapperSupplier().get().registerModule(new FrontEndJacksonModule());

        mvc =
            MockMvcBuilders
                .standaloneSetup(new UserFrontEndControllerServiceController(service))
                .setControllerAdvice(new ResponseCookieAdvice())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    @AfterEach
    public void
    tearDown()
    {
        backend.stop();
    }

    private MvcResult
    send(MockHttpServletRequestBuilder builder)
        throws Exception
    {
        MvcResult started = mvc.perform(builder.contentType(MediaType.APPLICATION_JSON).cookie(new Cookie("GRANITE_DID","device-1"))).andReturn();

        started.getAsyncResult();
        return mvc.perform(asyncDispatch(started)).andReturn();
    }

    private String
    loginBody()
    {
        return
            "{\"input\":{\"userId\":0,\"source\":\"Session.New\",\"command\":\"Login\",\"destination\":\"LoginView\","
                + "\"userName\":\"amy\",\"password\":\"" + PASSWORD + "\"}}";
    }

    @Test
    public void
    successfulLoginSetsTheSessionCookieOnTheHttpResponse()
        throws Exception
    {
        backend.respond(
            "login",
            new LoginReply()
                .setSessionData(
                    new SessionData().setSessionId(11L).setUserId(7L).setExpires(Instant.now().plusSeconds(1800)).setAuthorities(Set.of("ROLE_ADMIN")))
                .setAccessToken("access-token-secret")
                .setRefreshToken("refresh-token-secret")
                .setSessionHandle(HANDLE)
                .setSuccess(true));

        MvcResult result = send(post("/frontendcontroller-user-service/handle-input").content(loginBody()));

        String cookie = result.getResponse().getHeader("Set-Cookie");
        String body = result.getResponse().getContentAsString();

        assertTrue(cookie.startsWith("GRANITE_SID=" + HANDLE + ";"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("SameSite=Strict"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.matches(".*Max-Age=(17[0-9][0-9]|1800).*"));
        assertTrue(body.contains("AUTHENTICATED"));

        for (String secret : new String[] {HANDLE,"access-token-secret","refresh-token-secret",PASSWORD})
            assertFalse(body.contains(secret),secret);
    }

    @Test
    public void
    failedLoginHasNoCookie()
        throws Exception
    {
        backend.respond("login",new LoginReply().setSuccess(false).setFailureMessage("invalid credentials"));

        MvcResult result = send(post("/frontendcontroller-user-service/handle-input").content(loginBody()));

        assertNull(result.getResponse().getHeader("Set-Cookie"));
        assertTrue(result.getResponse().getContentAsString().contains("UNAUTHENTICATED"));
        assertFalse(result.getResponse().getContentAsString().contains(PASSWORD));
    }

    @Test
    public void
    logoutClearsTheCookie()
        throws Exception
    {
        backend.respond("logout",new LogoutReply().setSuccess(true));

        MvcResult result =
            send(
                post("/frontendcontroller-user-service/handle-input")
                    .header("Authorization",SessionRequestContextTest.bearer("{\"jti\":\"11\",\"sub\":\"7\"}"))
                    .content(
                        "{\"input\":{\"userId\":7,\"source\":\"Session.Current\",\"command\":\"Logout\","
                            + "\"destination\":\"LoginView\",\"logout\":true}}"));

        assertTrue(result.getResponse().getHeader("Set-Cookie").startsWith("GRANITE_SID=;"));
        assertTrue(result.getResponse().getHeader("Set-Cookie").contains("Max-Age=0"));
        assertTrue(backend.paths().get(0).endsWith("/session-command-service/logout"));
        assertTrue(backend.bodies().get(0).contains("\"sessionId\":11"));
        assertEquals(1,backend.paths().size());
    }
}

//////////////////////////////////////////////////////////////////////////////
