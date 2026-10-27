//////////////////////////////////////////////////////////////////////////////
// GatewayPreferencesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
@SpringBootTest(
    classes = SessionCookieGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "granite.gateway.authorization.role-rules.enabled=true")
public
class GatewayPreferencesTest
{
    private static final List<String> HOSTS = List.of("/frontendcontroller-user-service","/frontendcontroller-admin-service");
    private static final List<String> PATHS = List.of("/get-preferences-output","/handle-preferences-input");
    private static final String       CSRF  = "csrf-token-value";
    private static final String       BODY  = "{\"requestId\":\"p\"}";

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
    roleUserReachesPreferencesWithBearerOrCookie()
        throws Exception
    {
        String bearer = backend.sign(List.of("ROLE_USER"));
        String handle = handleFor("ROLE_USER");

        for (String host : HOSTS)
        {
            for (String path : PATHS)
            {
                assertEquals(200,postWithBearer(host + path,bearer),host + path);
                assertEquals(200,postWithCookie(host + path,handle,CSRF),host + path);
            }
        }
    }

    @Test
    public void
    anonymousIsUnauthorized()
    {
        for (String host : HOSTS)
            for (String path : PATHS)
                assertEquals(401,postWithBearer(host + path,null),host + path);
    }

    @Test
    public void
    roleUserIsStillDeniedUsersInput()
        throws Exception
    {
        String bearer = backend.sign(List.of("ROLE_USER"));

        for (String host : HOSTS)
        {
            assertEquals(403,postWithBearer(host + "/handle-users-input",bearer),host);
            assertEquals(403,postWithBearer(host + "/get-users-output",bearer),host);
        }
    }

    @Test
    public void
    csrfIsStillRequiredOnCookieRequests()
        throws Exception
    {
        String handle = handleFor("ROLE_USER");

        for (String host : HOSTS)
        {
            assertEquals(403,postWithCookie(host + "/handle-preferences-input",handle,null),host);
            assertEquals(403,postWithCookie(host + "/handle-preferences-input",handle,"wrong"),host);
        }
    }

    private static String
    handleFor(String authority)
        throws Exception
    {
        String handle = "h" + UUID.randomUUID().toString().replace("-","") + "handle";

        backend.registerHandle(handle,backend.sign(List.of(authority)));
        return handle;
    }

    private int
    postWithBearer(String path,String bearer)
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
                .bodyValue(BODY)
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }

    private int
    postWithCookie(String path,String handle,String csrfHeader)
    {
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
                    })
                .cookies(
                    cookies ->
                    {
                        cookies.add("GRANITE_SID",handle);
                        cookies.add("XSRF-TOKEN",CSRF);
                    })
                .bodyValue(BODY)
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }
}

//////////////////////////////////////////////////////////////////////////////
