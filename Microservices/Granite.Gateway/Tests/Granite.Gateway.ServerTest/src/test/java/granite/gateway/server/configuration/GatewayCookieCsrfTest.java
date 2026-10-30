//////////////////////////////////////////////////////////////////////////////
// GatewayCookieCsrfTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import granite.foundation.core.security.GraniteCookieNames;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = SessionCookieGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "granite.gateway.authorization.role-rules.enabled=true")
public
class GatewayCookieCsrfTest
{
    private static final GraniteCookieNames NAMES = new GraniteCookieNames("GRANITE");

    private static final String GET_USERS_OUTPUT     = "/frontendcontroller-user-service/get-users-output";
    private static final String GET_SESSION_OUTPUT   = "/frontendcontroller-user-service/get-session-output";
    private static final String HANDLE_SESSION_INPUT = "/frontendcontroller-user-service/handle-session-input";

    private static GatewayTestBackend backend;

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @DynamicPropertySource
    static void
    backendProperties(DynamicPropertyRegistry registry)
        throws Exception
    {
        backend = new GatewayTestBackend();
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
    deviceCookieIssuedWhenMissing()
    {
        ResponseCookie device = cookies(get(Map.of())).getFirst(NAMES.getDevice());

        assertNotNull(device);
        assertTrue(device.getValue().matches("[A-Za-z0-9_-]{43}"),device.getValue());
        assertTrue(device.isHttpOnly());
        assertTrue(device.isSecure());
        assertEquals("Lax",device.getSameSite());
        assertEquals("/",device.getPath());
        assertEquals(Duration.ofSeconds(31536000),device.getMaxAge());
    }

    @Test
    public void
    deviceCookieNotReissuedWhenPresent()
    {
        assertNull(cookies(get(Map.of(NAMES.getDevice(),"existing-device"))).getFirst(NAMES.getDevice()));
    }

    @Test
    public void
    deviceCookieAndCsrfCookieNotIssuedToBearerRequests()
        throws Exception
    {
        String                       token  = sign("ROLE_ADMIN");
        EntityExchangeResult<byte[]> result =
            client
                .post()
                .uri("/user-command-service/create-user")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue("{}")
                .exchange()
                .expectBody()
                .returnResult();

        assertEquals(200,result.getStatus().value());
        assertNull(cookies(result).getFirst(NAMES.getDevice()));
        assertNull(cookies(result).getFirst(NAMES.getCsrf()));
    }

    @Test
    public void
    newDeviceCookieIsForwardedOnFirstRequest()
    {
        EntityExchangeResult<byte[]> result =
            post(GET_SESSION_OUTPUT,GatewayPublicInputTest.output("Session.New",null,"LoginView"),Map.of(),null,null);
        String                       device = cookies(result).getFirst(NAMES.getDevice()).getValue();

        assertEquals(200,result.getStatus().value());
        assertTrue(backend.getReceivedCookies().stream().anyMatch(c -> c.contains(NAMES.getDevice() + "=" + device)));
    }

    @Test
    public void
    csrfCookieIssuedReadableByScript()
    {
        ResponseCookie csrf = cookies(get(Map.of())).getFirst(NAMES.getCsrf());

        assertNotNull(csrf);
        assertFalse(csrf.getValue().isBlank());
        assertFalse(csrf.isHttpOnly());
        assertEquals("/",csrf.getPath());
        assertEquals("Strict",csrf.getSameSite());
    }

    @Test
    public void
    deviceCookieIsHostPrefixed()
    {
        String header = setCookie(get(Map.of()),"__Host-GRANITE_DID=");

        assertTrue(header.contains("Secure"),header);
        assertTrue(header.contains("Path=/"),header);
        assertFalse(header.contains("Domain"),header);
    }

    @Test
    public void
    csrfCookieIsHostPrefixed()
    {
        String header = setCookie(get(Map.of()),"__Host-GRANITE-XSRF-TOKEN=");

        assertTrue(header.contains("Secure"),header);
        assertTrue(header.contains("Path=/"),header);
        assertFalse(header.contains("Domain"),header);
    }

    private static String
    setCookie(EntityExchangeResult<byte[]> result,String prefix)
    {
        List<String> headers = result.getResponseHeaders().get("Set-Cookie");

        assertNotNull(headers);

        return
            headers
                .stream()
                .filter(h -> h.startsWith(prefix))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no Set-Cookie starting with " + prefix + ": " + headers));
    }

    @Test
    public void
    cookiePostWithoutCsrfTokenIsForbidden()
        throws Exception
    {
        String handle = register("ROLE_ADMIN");

        assertEquals(403,post(GET_USERS_OUTPUT,users(),Map.of(NAMES.getSession(),handle),null,null).getStatus().value());
        assertFalse(backend.getResolveCalls().contains(handle));
    }

    @Test
    public void
    cookiePostWithWrongCsrfTokenIsForbidden()
        throws Exception
    {
        String handle = register("ROLE_ADMIN");

        assertEquals(
            403,
            post(GET_USERS_OUTPUT,users(),Map.of(NAMES.getSession(),handle,NAMES.getCsrf(),"right"),"wrong",null)
                .getStatus()
                .value());
        assertFalse(backend.getResolveCalls().contains(handle));
    }

    @Test
    public void
    cookiePostWithMatchingCsrfTokenPasses()
        throws Exception
    {
        String handle = register("ROLE_ADMIN");

        assertEquals(
            200,
            post(GET_USERS_OUTPUT,users(),Map.of(NAMES.getSession(),handle,NAMES.getCsrf(),"right"),"right",null)
                .getStatus()
                .value());
    }

    @Test
    public void
    publicLoginWithCookiesNeedsCsrfToken()
    {
        String login = GatewayPublicInputTest.input("Session.New","Login","LoginView");

        assertEquals(403,post(HANDLE_SESSION_INPUT,login,Map.of(NAMES.getDevice(),"d"),null,null).getStatus().value());
        assertEquals(
            200,
            post(HANDLE_SESSION_INPUT,login,Map.of(NAMES.getDevice(),"d",NAMES.getCsrf(),"t"),"t",null).getStatus().value());
    }

    @Test
    public void
    getWithCookiesNeedsNoCsrfToken()
        throws Exception
    {
        String handle = register("ROLE_ADMIN");
        int    status =
            client
                .method(HttpMethod.GET)
                .uri("/user-query-service/find-users-by-key")
                .cookie(NAMES.getSession(),handle)
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();

        assertEquals(200,status);
    }

    @Test
    public void
    bearerPostWithCookiesNeedsNoCsrfToken()
        throws Exception
    {
        assertEquals(
            200,
            post(GET_USERS_OUTPUT,users(),Map.of("other","x"),null,sign("ROLE_ADMIN")).getStatus().value());
    }

    @Test
    public void
    postWithoutCookiesNeedsNoCsrfToken()
    {
        int status =
            post(GET_SESSION_OUTPUT,GatewayPublicInputTest.output("Session.New",null,"LoginView"),Map.of(),null,null)
                .getStatus()
                .value();

        assertNotEquals(401,status);
        assertNotEquals(403,status);
    }

    @Test
    public void
    forbiddenResponseCarriesFreshCsrfCookie()
    {
        EntityExchangeResult<byte[]> result = post(GET_USERS_OUTPUT,users(),Map.of(NAMES.getDevice(),"d"),null,null);

        assertEquals(403,result.getStatus().value());
        assertNotNull(cookies(result).getFirst(NAMES.getCsrf()));
    }

    private EntityExchangeResult<byte[]>
    get(Map<String,String> cookies)
    {
        return
            client
                .get()
                .uri("/health/liveness")
                .cookies(jar -> cookies.forEach(jar::add))
                .exchange()
                .expectBody()
                .returnResult();
    }

    private EntityExchangeResult<byte[]>
    post(String path,String body,Map<String,String> cookies,String csrfHeader,String bearer)
    {
        Map<String,String> ordered = new LinkedHashMap<>(cookies);

        return
            client
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(
                    headers ->
                    {
                        if (csrfHeader != null)
                            headers.set("X-XSRF-TOKEN",csrfHeader);

                        if (bearer != null)
                            headers.setBearerAuth(bearer);
                    })
                .cookies(jar -> ordered.forEach(jar::add))
                .bodyValue(body)
                .exchange()
                .expectBody()
                .returnResult();
    }

    private static MultiValueMap<String,ResponseCookie>
    cookies(EntityExchangeResult<byte[]> result)
    {
        return result.getResponseCookies();
    }

    private static String
    register(String authority)
        throws Exception
    {
        String handle = "h" + UUID.randomUUID().toString().replace("-","");

        backend.registerHandle(handle,sign(authority));
        return handle;
    }

    private static String
    sign(String authority)
        throws Exception
    {
        return backend.sign(List.of(authority));
    }

    private static String
    users()
    {
        return GatewayPublicInputTest.output("User.All",null,"UsersEditableView");
    }
}

//////////////////////////////////////////////////////////////////////////////
