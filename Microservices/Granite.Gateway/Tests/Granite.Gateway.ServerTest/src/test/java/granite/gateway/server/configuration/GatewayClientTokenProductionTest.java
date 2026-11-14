//////////////////////////////////////////////////////////////////////////////
// GatewayClientTokenProductionTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
@ActiveProfiles("production")
@SpringBootTest(
    properties = {
        "granite.security.jwt.issuer=granite-session",
        "granite.security.jwt.audience=granite-services"},
    classes = ClientGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GatewayClientTokenProductionTest
{
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
    clientAdminClientMayRotateOwnSecretInProduction()
        throws Exception
    {
        String path = "/client-command-service/rotate-own-client-secret";

        assertEquals(200,post(path,backend.signClientToken("gc_p",List.of("ROLE_CLIENT_ADMIN"))));
        assertEquals(403,post(path,backend.signClientToken("gc_p",List.of("ROLE_USER"))));
        assertEquals(
            403,
            post("/client-command-service/create-client",backend.signClientToken("gc_p",List.of("ROLE_CLIENT_ADMIN"))));
    }

    @Test
    public void
    tokenEndpointIsPublicInProduction()
    {
        assertEquals(200,post("/session-command-service/oauth2/token",null));
    }

    private int
    post(String path,String token)
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
                .bodyValue("{}")
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }
}

//////////////////////////////////////////////////////////////////////////////
