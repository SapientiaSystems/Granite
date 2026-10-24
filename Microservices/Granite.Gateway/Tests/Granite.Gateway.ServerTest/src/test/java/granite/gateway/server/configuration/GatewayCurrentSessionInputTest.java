//////////////////////////////////////////////////////////////////////////////
// GatewayCurrentSessionInputTest.java
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

import static granite.gateway.server.configuration.GatewayPublicInputTest.input;
import static granite.gateway.server.configuration.GatewayPublicInputTest.output;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = SessionCookieGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "granite.gateway.authorization.role-rules.enabled=true")
public
class GatewayCurrentSessionInputTest
{
    private static final List<String> HOSTS = List.of("/frontendcontroller-user-service","/frontendcontroller-admin-service");
    private static final String       CSRF  = "csrf-token-value";

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
    auditorCanLogOutWithCookieOrBearer()
        throws Exception
    {
        for (String host : HOSTS)
        {
            String cookieBody = logout("cookie-" + UUID.randomUUID());
            String bearerBody = logout("bearer-" + UUID.randomUUID());

            assertEquals(200,postWithCookie(host + "/handle-input",cookieBody,handleFor("ROLE_AUDITOR")));
            assertTrue(backend.getReceivedBodies().contains(cookieBody));
            assertEquals(200,postWithBearer(host + "/handle-input",bearerBody,backend.sign(List.of("ROLE_AUDITOR"))));
            assertTrue(backend.getReceivedBodies().contains(bearerBody));
        }
    }

    @Test
    public void
    roleUserCanLogOutAndReadSessionView()
        throws Exception
    {
        String handle = handleFor("ROLE_USER");
        String bearer = backend.sign(List.of("ROLE_USER"));

        for (String host : HOSTS)
        {
            assertEquals(200,postWithCookie(host + "/handle-input",logout("u"),handle));
            assertEquals(200,postWithCookie(host + "/get-output",sessionView(),handle));
            assertEquals(200,postWithBearer(host + "/handle-input",logout("u"),bearer));
            assertEquals(200,postWithBearer(host + "/get-output",sessionView(),bearer));
        }
    }

    @Test
    public void
    auditorIsStillDeniedOtherInputs()
        throws Exception
    {
        String handle = handleFor("ROLE_AUDITOR");
        String bearer = backend.sign(List.of("ROLE_AUDITOR"));

        for (String host : HOSTS)
            for (String body : List.of(
                input("User.All","Save","UsersEditableView"),
                input("User.All","Delete","UsersEditableView"),
                input("Session.Current","Logout","SessionView")))
            {
                assertEquals(403,postWithCookie(host + "/handle-input",body,handle),host + " " + body);
                assertEquals(403,postWithBearer(host + "/handle-input",body,bearer),host + " " + body);
            }
    }

    @Test
    public void
    anonymousLogoutIsUnauthorized()
    {
        for (String host : HOSTS)
        {
            assertEquals(401,postWithBearer(host + "/handle-input",logout("u"),null));
            assertEquals(401,postWithBearer(host + "/get-output",sessionView(),null));
        }
    }

    @Test
    public void
    spoofedKeysDoNotMatch()
        throws Exception
    {
        String handle = handleFor("ROLE_USER");
        String bearer = backend.sign(List.of("ROLE_USER"));

        List<String> inputs =
            List.of(
                input("Session.Current","Logout","SessionView"),
                input("Session.Current","Save","LoginView"),
                input("Session.Current",null,"LoginView"),
                input("Session.Current",null,"SessionView"),
                input("Session.Current->Logout",null,"LoginView"),
                input("Session.Current","Logout->LoginView",null),
                output("Session.Current","Logout","LoginView"),
                "{\"input\":{\"source\":\"Session.Current\",\"command\":7,\"destination\":\"LoginView\"}}",
                "{\"input\":{\"source\":\"Session.Current\",\"command\":{\"name\":\"Logout\"},\"destination\":\"LoginView\"}}");
        List<String> outputs =
            List.of(
                output("Session.Current","Logout","SessionView"),
                output("Session.Current","Logout","LoginView"),
                output("Session.Current",null,"LoginView"),
                output("Session.Current->SessionView",null,null),
                input("Session.Current",null,"SessionView"),
                "{\"source\":\"Session.Current\",\"command\":7,\"destination\":\"SessionView\"}");

        for (String host : HOSTS)
        {
            for (String body : inputs)
            {
                assertEquals(403,postWithCookie(host + "/handle-input",body,handle),host + " " + body);
                assertEquals(403,postWithBearer(host + "/handle-input",body,bearer),host + " " + body);
            }

            for (String body : outputs)
            {
                assertEquals(403,postWithCookie(host + "/get-output",body,handle),host + " " + body);
                assertEquals(403,postWithBearer(host + "/get-output",body,bearer),host + " " + body);
            }
        }
    }

    private static String
    logout(String requestId)
    {
        return
            "{\"requestId\":\"" + requestId + "\",\"input\":{"
                + "\"source\":\"Session.Current\",\"command\":\"Logout\",\"destination\":\"LoginView\"}}";
    }

    private static String
    sessionView()
    {
        return output("Session.Current",null,"SessionView");
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
    postWithBearer(String path,String body,String bearer)
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
    postWithCookie(String path,String body,String handle)
    {
        return
            client
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.set("X-XSRF-TOKEN",CSRF))
                .cookies(
                    cookies ->
                    {
                        cookies.add("GRANITE_SID",handle);
                        cookies.add("XSRF-TOKEN",CSRF);
                    })
                .bodyValue(body)
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }
}

//////////////////////////////////////////////////////////////////////////////
