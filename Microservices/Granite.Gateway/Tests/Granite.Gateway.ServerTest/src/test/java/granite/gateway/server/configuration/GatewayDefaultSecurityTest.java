//////////////////////////////////////////////////////////////////////////////
// GatewayDefaultSecurityTest.java
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

@Tag("CommitStage")
@SpringBootTest(
    classes = GatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GatewayDefaultSecurityTest
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
    defaultChainRequiresAuthenticationButNoAuthority()
        throws Exception
    {
        post("/user-command-service/create-user",null).expectStatus().isUnauthorized();
        post("/user-command-service/create-user",backend.sign(List.of())).expectStatus().isOk();
    }

    @Test
    public void
    defaultChainKeepsSessionEntryPointsPublic()
    {
        post("/session-command-service/login",null).expectStatus().isOk();
        post("/session-command-service/login-with-oauth2",null).expectStatus().isOk();
        post("/session-command-service/refresh",null).expectStatus().isOk();
        post("/session-command-service/logout",null).expectStatus().isUnauthorized();
    }

    @Test
    public void
    healthAndPreflightArePublic()
    {
        client.get().uri("/health/liveness").exchange().expectStatus().value(status -> assertNot401(status));
        client
            .options()
            .uri("/user-command-service/create-user")
            .exchange()
            .expectStatus().value(status -> assertNot401(status));
    }

    @Test
    public void
    swaggerIsPublicOutsideProduction()
    {
        client
            .get()
            .uri("/session-command-service/swagger-ui/index.html")
            .exchange()
            .expectStatus().value(status -> assertNot401(status));
    }

    private static void
    assertNot401(int status)
    {
        if (status == 401 || status == 403)
            throw new AssertionError("expected a public path but got " + status);
    }

    private WebTestClient.ResponseSpec
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
                .exchange();
    }
}

//////////////////////////////////////////////////////////////////////////////
