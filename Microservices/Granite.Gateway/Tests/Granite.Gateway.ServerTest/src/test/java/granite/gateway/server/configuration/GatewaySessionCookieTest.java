//////////////////////////////////////////////////////////////////////////////
// GatewaySessionCookieTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = SessionCookieGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "granite.gateway.authorization.role-rules.enabled=true",
        "granite.gateway.session-resolve.timeout=PT0.5S"})
public
class GatewaySessionCookieTest
{
    private static final String GET_USERS_OUTPUT     = "/frontendcontroller-user-service/get-users-output";
    private static final String HANDLE_SESSION_INPUT = "/frontendcontroller-user-service/handle-session-input";
    private static final String CSRF         = "csrf-token-value";

    private static GatewayTestBackend backend;
    private static String             serviceToken;

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @DynamicPropertySource
    static void
    backendProperties(DynamicPropertyRegistry registry)
        throws Exception
    {
        backend      = new GatewayTestBackend();
        serviceToken = backend.signServiceToken(List.of("SCOPE_internal"));
        backend.registerProperties(registry);
    }

    @AfterAll
    public static void
    stopBackend()
    {
        backend.stop();
    }

    @BeforeEach
    public void
    setUp()
    {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    public void
    cookieRequestReachesFecWithJwtAndWithoutSid()
        throws Exception
    {
        String handle = newHandle();
        String token  = register(handle,"ROLE_ADMIN");
        String device = "device-" + UUID.randomUUID();

        assertEquals(200,post(GET_USERS_OUTPUT,users(),handle,device,null));
        assertTrue(backend.getReceivedAuthorizations().contains("Bearer " + token));

        String cookies = lastCookieHeaderWith(device);

        assertTrue(cookies.contains("GRANITE_DID=" + device),cookies);
        assertTrue(cookies.contains("other=kept"),cookies);
        assertTrue(cookies.contains("XSRF-TOKEN=" + CSRF),cookies);
        assertFalse(cookies.contains("GRANITE_SID"),cookies);
        assertFalse(cookies.contains(handle),cookies);
        assertTrue(backend.getResolveAuthorizations().stream().allMatch("null"::equals));
    }

    @Test
    public void
    resolveSessionIsDeniedExternallyAndNeverReachesTheBackend()
        throws Exception
    {
        String handle  = newHandle();
        String device  = "device-" + UUID.randomUUID();
        String path    = "/session-command-service/resolve-session";
        String body    = "{\"handle\":\"" + handle + "\"}";
        String admin   = backend.sign(List.of("ROLE_ADMIN"));
        String service = backend.signServiceToken(List.of("SCOPE_internal"));
        int    before  = backend.getResolveCalls().size();

        register(handle,"ROLE_ADMIN");
        assertEquals(403,post(path,body,handle,device,admin));
        assertEquals(403,post(path,body,handle,device,service));
        assertEquals(before,backend.getResolveCalls().size());
        assertEquals(401,postWithoutCookies(path,body,null));
        assertEquals(403,postWithoutCookies(path,body,admin));
        assertEquals(before,backend.getResolveCalls().size());
        assertFalse(backend.getResolveCalls().contains(handle));
    }

    @Test
    public void
    oktaPathsAreDeniedToBrowserSessionsAndOpenToServiceTokens()
        throws Exception
    {
        String admin   = backend.sign(List.of("ROLE_ADMIN"));
        String service = backend.signClientServiceToken("granite-frontendcontroller");
        String handle  = newHandle();
        String device  = "device-" + UUID.randomUUID();

        register(handle,"ROLE_ADMIN");

        for (String path : List.of("/session-command-service/start-okta-login","/session-command-service/complete-okta-login"))
        {
            int before = backend.getReceivedBodies().size();

            assertEquals(401,postWithoutCookies(path,"{}",null),path);
            assertEquals(403,postWithoutCookies(path,"{}",admin),path);
            assertEquals(403,post(path,"{}",handle,device,null),path);
            assertEquals(before,backend.getReceivedBodies().size(),path);
            assertEquals(200,postWithoutCookies(path,"{}",service),path);
            assertEquals(before + 1,backend.getReceivedBodies().size(),path);
        }
    }

    @Test
    public void
    unknownClosedOrExpiredHandleIsUnauthorized()
    {
        assertEquals(401,post(GET_USERS_OUTPUT,users(),newHandle(),null,null));
    }

    @Test
    public void
    resolveErrorIsUnauthorized()
        throws Exception
    {
        String handle = newHandle();

        register(handle,"ROLE_ADMIN");
        backend.failResolvesFor(handle);
        assertEquals(401,post(GET_USERS_OUTPUT,users(),handle,null,null));
    }

    @Test
    public void
    resolveTimeoutIsUnauthorized()
        throws Exception
    {
        String handle = newHandle();

        register(handle,"ROLE_ADMIN");
        backend.delayResolvesFor(handle);
        assertEquals(401,post(GET_USERS_OUTPUT,users(),handle,null,null));
    }

    @Test
    public void
    resolvedJwtStillPassesRoleRules()
        throws Exception
    {
        String handle = newHandle();

        register(handle,"ROLE_USER");
        assertEquals(403,post(GET_USERS_OUTPUT,users(),handle,null,null));
    }

    @Test
    public void
    resolvedJwtStillPassesRevocationCheck()
        throws Exception
    {
        String handle    = newHandle();
        String sessionId = backend.newSessionId();

        backend.registerHandle(handle,backend.sign(sessionId,List.of("ROLE_ADMIN")));
        backend.revoke(sessionId);
        assertEquals(401,post(GET_USERS_OUTPUT,users(),handle,null,null));
    }

    @Test
    public void
    bearerTakesPrecedenceOverCookie()
        throws Exception
    {
        String handle = newHandle();
        String device = "device-" + UUID.randomUUID();
        String bearer = backend.sign(List.of("ROLE_ADMIN"));

        register(handle,"ROLE_USER");
        assertEquals(200,post(GET_USERS_OUTPUT,users(),handle,device,bearer));
        assertTrue(backend.getReceivedAuthorizations().contains("Bearer " + bearer));
        assertFalse(backend.getResolveCalls().contains(handle));
        assertFalse(lastCookieHeaderWith(device).contains("GRANITE_SID"));
    }

    @Test
    public void
    publicInputWithDeadCookieIsForwardedWithoutResolving()
    {
        String handle = newHandle();
        String device = "device-" + UUID.randomUUID();
        int    status =
            post(HANDLE_SESSION_INPUT,GatewayPublicInputTest.input("Session.New","Login","LoginView"),handle,device,null);

        assertNotEquals(401,status);
        assertNotEquals(403,status);
        assertFalse(backend.getResolveCalls().contains(handle));
        assertFalse(lastCookieHeaderWith(device).contains("GRANITE_SID"));
    }

    @Test
    public void
    cachedHandleIsResolvedOnce()
        throws Exception
    {
        String handle = newHandle();

        register(handle,"ROLE_ADMIN");
        assertEquals(200,post(GET_USERS_OUTPUT,users(),handle,null,null));
        assertEquals(200,post(GET_USERS_OUTPUT,users(),handle,null,null));
        assertEquals(1,Collections.frequency(backend.getResolveCalls(),handle));
    }

    @Test
    public void
    neverLogsHandleOrToken()
        throws Exception
    {
        Logger                     root     = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        Logger                     gateway  = (Logger)LoggerFactory.getLogger("granite.gateway");
        Level                      previous = gateway.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        String                     good     = newHandle();
        String                     failing  = newHandle();
        String                     unknown  = newHandle();
        String                     token    = register(good,"ROLE_ADMIN");

        register(failing,"ROLE_ADMIN");
        backend.failResolvesFor(failing);
        appender.start();
        root.addAppender(appender);
        gateway.setLevel(Level.DEBUG);

        try
        {
            post(GET_USERS_OUTPUT,users(),good,null,null);
            post(GET_USERS_OUTPUT,users(),failing,null,null);
            post(GET_USERS_OUTPUT,users(),unknown,null,null);
        }
        finally
        {
            root.detachAppender(appender);
            gateway.setLevel(previous);
        }

        for (ILoggingEvent event : appender.list)
            for (String secret : List.of(good,failing,unknown,token,serviceToken,CSRF))
                assertFalse(event.getFormattedMessage().contains(secret),event.getFormattedMessage());
    }

    private static String
    register(String handle,String authority)
        throws Exception
    {
        String token = backend.sign(List.of(authority));

        backend.registerHandle(handle,token);
        return token;
    }

    private static String
    lastCookieHeaderWith(String marker)
    {
        List<String> cookies = backend.getReceivedCookies();

        for (int i = cookies.size() - 1; i >= 0; i--)
            if (cookies.get(i).contains(marker))
                return cookies.get(i);

        throw new AssertionError("no forwarded request carried " + marker);
    }

    private static String
    newHandle()
    {
        return "h" + UUID.randomUUID().toString().replace("-","") + "handle";
    }

    private static String
    users()
    {
        return GatewayPublicInputTest.output("User.All",null,"UsersEditableView");
    }

    private int
    postWithoutCookies(String path,String body,String bearer)
    {
        return
            client
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(
                    headers ->
                    {
                        if (bearer != null)
                            headers.setBearerAuth(bearer);
                    })
                .bodyValue(body)
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }

    private int
    post(String path,String body,String handle,String device,String bearer)
    {
        return
            client
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(
                    headers ->
                    {
                        if (bearer != null)
                            headers.setBearerAuth(bearer);

                        headers.set("X-XSRF-TOKEN",CSRF);
                    })
                .cookies(
                    cookies ->
                    {
                        cookies.add("GRANITE_SID",handle);
                        cookies.add("other","kept");
                        cookies.add("XSRF-TOKEN",CSRF);

                        if (device != null)
                            cookies.add("GRANITE_DID",device);
                    })
                .bodyValue(body)
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }
}

//////////////////////////////////////////////////////////////////////////////
