//////////////////////////////////////////////////////////////////////////////
// GatewayCookiePrefixTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import granite.foundation.core.security.GraniteCookieNames;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = SessionCookieGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "granite.cookies.prefix=POPLAR",
        "granite.gateway.authorization.role-rules.enabled=true"})
public
class GatewayCookiePrefixTest
{
    private static final GraniteCookieNames POPLAR  = new GraniteCookieNames("POPLAR");
    private static final GraniteCookieNames GRANITE = new GraniteCookieNames("GRANITE");
    private static final String             CSRF    = "csrf-token-value";
    private static final String             PATH    = "/frontendcontroller-user-service/get-users-output";

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
    customPrefixReadsPrefixedSessionCookie()
        throws Exception
    {
        String handle = newHandle();
        String other  = newHandle();

        backend.registerHandle(handle,backend.sign(List.of("ROLE_ADMIN")));
        backend.registerHandle(other,backend.sign(List.of("ROLE_ADMIN")));

        assertEquals(200,post(POPLAR.getSession(),handle));
        assertEquals(401,post(GRANITE.getSession(),other));
        assertTrue(backend.getResolveCalls().contains(handle));
        assertFalse(backend.getResolveCalls().contains(other));
    }

    @Test
    public void
    customPrefixIssuesPrefixedDeviceAndCsrfCookies()
    {
        List<String> headers =
            client
                .get()
                .uri("/health/liveness")
                .exchange()
                .returnResult(String.class)
                .getResponseHeaders()
                .get("Set-Cookie");

        assertTrue(headers.stream().anyMatch(h -> h.startsWith("__Host-POPLAR_DID=")),headers.toString());
        assertTrue(headers.stream().anyMatch(h -> h.startsWith("__Host-POPLAR-XSRF-TOKEN=")),headers.toString());
    }

    private static String
    newHandle()
    {
        return "h" + UUID.randomUUID().toString().replace("-","") + "handle";
    }

    private int
    post(String sessionCookie,String handle)
    {
        return
            client
                .post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.set("X-XSRF-TOKEN",CSRF))
                .cookies(
                    cookies ->
                    {
                        cookies.add(sessionCookie,handle);
                        cookies.add(POPLAR.getCsrf(),CSRF);
                    })
                .bodyValue(GatewayPublicInputTest.output("User.All",null,"UsersEditableView"))
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }
}

//////////////////////////////////////////////////////////////////////////////
