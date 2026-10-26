//////////////////////////////////////////////////////////////////////////////
// GatewayPublicInputTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = RoleRulesGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "granite.gateway.authorization.role-rules.enabled=true")
public
class GatewayPublicInputTest
{
    private static final List<String> HOSTS = List.of("/frontendcontroller-user-service","/frontendcontroller-admin-service");

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
    loginViewOutputIsPublicOnBothHosts()
    {
        for (String host : HOSTS)
            assertPublic(host + "/get-session-output",output("Session.New",null,"LoginView"));
    }

    @Test
    public void
    loginInputsArePublicOnBothHosts()
    {
        for (String host : HOSTS)
            for (String command : List.of("Login","StartOktaLogin","CompleteOktaLogin"))
                assertPublic(host + "/handle-session-input",input("Session.New",command,"LoginView"));
    }

    @Test
    public void
    otherKeysStayProtected()
    {
        for (String host : HOSTS)
        {
            assertEquals(401,post(host + "/get-users-output",output("User.All",null,"UsersEditableView")));
            assertEquals(401,post(host + "/handle-users-input",input("User.All","Save","UsersEditableView")));
            assertEquals(401,post(host + "/handle-session-input",input("Session.New",null,"LoginView")));
            assertEquals(401,post(host + "/get-session-output",output("Session.New","Login","LoginView")));
            assertEquals(401,post(host + "/handle-session-input",input("Session.Current","Logout","LoginView")));
            assertEquals(401,post(host + "/handle-session-input","not json"));
        }
    }

    @Test
    public void
    inspectedBodyIsForwardedUnchanged()
    {
        String body = input("Session.New","Login","LoginView");

        assertPublic("/frontendcontroller-user-service/handle-session-input",body);
        assertTrue(backend.getReceivedBodies().contains(body));
    }

    @Test
    public void
    staticAppPathsArePublic()
    {
        int status =
            client
                .get()
                .uri("/index.html")
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();

        assertNotEquals(401,status);
        assertNotEquals(403,status);
    }

    @Test
    public void
    staticAppPathsAreListedAsPublic()
    {
        List<String> paths = new GraniteGatewayServerConfiguration().getPublicPathPatterns(new MockEnvironment());

        assertTrue(paths.containsAll(List.of("/","/index.html","/config.js","/favicon.ico","/assets/**")));
    }

    private void
    assertPublic(String path,String body)
    {
        int status = post(path,body);

        assertNotEquals(401,status,path + " " + body);
        assertNotEquals(403,status,path + " " + body);
    }

    private int
    post(String path,String body)
    {
        return
            client
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }

    static String
    output(String source,String command,String destination)
    {
        return
            "{\"requestId\":\"6c1e9f0e-0f43-4a43-9f38-0d5a3e1f8a11\""
                + field("source",source)
                + field("command",command)
                + field("destination",destination)
                + "}";
    }

    static String
    input(String source,String command,String destination)
    {
        return
            "{\"requestId\":\"6c1e9f0e-0f43-4a43-9f38-0d5a3e1f8a11\",\"input\":{\"userName\":\"u\""
                + field("source",source)
                + field("command",command)
                + field("destination",destination)
                + "}}";
    }

    private static String
    field(String name,String value)
    {
        return value == null ? "" : ",\"" + name + "\":\"" + value + "\"";
    }
}

//////////////////////////////////////////////////////////////////////////////
