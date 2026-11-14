//////////////////////////////////////////////////////////////////////////////
// GatewayClientTokenTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import com.nimbusds.jwt.SignedJWT;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("CommitStage")
@SpringBootTest(
    classes = ClientGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GatewayClientTokenTest
{
    private static final String ROTATE     = "/client-command-service/rotate-own-client-secret";
    private static final String TOKEN_PATH = "/session-command-service/oauth2/token";
    private static final String FEC        = "/frontendcontroller-user-service/";

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
    clientTokenSkipsRevocation()
        throws Exception
    {
        String token = backend.signClientToken("gc_a",List.of("ROLE_USER"));
        String jti   = SignedJWT.parse(token).getJWTClaimsSet().getJWTID();

        backend.revoke(jti);
        assertEquals(200,post("/poplar-service/x",token,"{}"));
        assertFalse(backend.getSessionLookups().contains(jti));
    }

    @Test
    public void
    clientTokenRefusedOnFrontEndAndIdentityPaths()
        throws Exception
    {
        String token = backend.signClientToken("gc_b",List.of("ROLE_AUDITOR","ROLE_USER"));

        for (String path :
                List.of(
                    FEC + "get-preferences-output",
                    FEC + "get-users-output",
                    "/session-command-service/logout",
                    "/user-query-service/x",
                    "/client-query-service/x"))
            assertEquals(403,post(path,token,"{}"),path);
    }

    @Test
    public void
    clientAdminClientMayRotateOwnSecret()
        throws Exception
    {
        assertEquals(200,post(ROTATE,backend.signClientToken("gc_c",List.of("ROLE_CLIENT_ADMIN")),"{}"));
        assertEquals(403,post(ROTATE,backend.signClientToken("gc_c",List.of("ROLE_USER")),"{}"));
    }

    @Test
    public void
    clientTokenUsesApplicationRules()
        throws Exception
    {
        assertEquals(200,post("/poplar-service/x",backend.signClientToken("gc_d",List.of("ROLE_USER")),"{}"));
        assertEquals(403,post("/poplar-service/x",backend.signClientToken("gc_d",List.of("ROLE_AUDITOR")),"{}"));
    }

    @Test
    public void
    tokenEndpointPublicAndRateLimited()
    {
        for (int i = 0; i < 10; i++)
            assertEquals(200,post(TOKEN_PATH,null,"grant_type=client_credentials"),"request " + i);

        assertEquals(429,post(TOKEN_PATH,null,"grant_type=client_credentials"));
    }

    @Test
    public void
    clientFrontEndRules()
        throws Exception
    {
        String reader = backend.sign(List.of("ROLE_AUDITOR"));
        String owner  = backend.sign(List.of("ROLE_CLIENT_ADMIN"));
        String admin  = backend.sign(List.of("ROLE_ADMIN"));

        assertEquals(200,post(FEC + "get-clients-output",reader,"{}"));
        assertEquals(403,post(FEC + "handle-clients-input",reader,"{}"));
        assertEquals(403,post(FEC + "get-owned-clients-output",reader,"{}"));
        assertEquals(200,post(FEC + "get-owned-clients-output",owner,"{}"));
        assertEquals(200,post(FEC + "handle-owned-client-secret-input",owner,"{}"));
        assertEquals(403,post(FEC + "get-clients-output",owner,"{}"));
        assertEquals(403,post(FEC + "handle-clients-input",owner,"{}"));
        assertEquals(200,post(FEC + "handle-clients-input",admin,"{}"));
        assertEquals(200,post(FEC + "get-owned-clients-output",admin,"{}"));
        assertEquals(200,post("/client-query-service/x",reader,"{}"));
        assertEquals(403,post("/client-command-service/x",reader,"{}"));
        assertEquals(200,post("/client-command-service/x",admin,"{}"));
    }

    @Test
    public void
    clientApplicationSessionConfined()
        throws Exception
    {
        List<String> authorities = List.of("ROLE_ADMIN","ROLE_CLIENT_ADMIN");
        String       confined    = backend.sign(authorities,"client");
        String       plain       = backend.sign(authorities);
        String       logout      =
            "{\"input\":{\"source\":\"Session.Current\",\"command\":\"Logout\",\"destination\":\"LoginView\"}}";

        assertEquals(200,post(FEC + "get-owned-clients-output",confined,"{}"));
        assertEquals(200,post(FEC + "handle-owned-client-secret-input",confined,"{}"));
        assertEquals(200,post(FEC + "handle-session-input",confined,logout));
        assertEquals(403,post(FEC + "handle-users-input",confined,"{}"));
        assertEquals(403,post(FEC + "get-preferences-output",confined,"{}"));
        assertEquals(403,post("/user-query-service/x",confined,"{}"));
        assertEquals(200,post(FEC + "handle-users-input",plain,"{}"));
        assertEquals(200,post("/user-query-service/x",plain,"{}"));
    }

    private int
    post(String path,String token,String body)
    {
        return
            client
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(
                    headers ->
                    {
                        if (token != null)
                            headers.setBearerAuth(token);
                    })
                .bodyValue(body)
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }
}

//////////////////////////////////////////////////////////////////////////////
