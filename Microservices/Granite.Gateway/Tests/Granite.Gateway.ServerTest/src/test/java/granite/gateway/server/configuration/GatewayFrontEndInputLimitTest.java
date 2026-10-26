//////////////////////////////////////////////////////////////////////////////
// GatewayFrontEndInputLimitTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@Tag("CommitStage")
@SpringBootTest(
    classes = RoleRulesGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "granite.gateway.authorization.role-rules.enabled=true",
        "granite.gateway.front-end-input.max-bytes=64"})
public
class GatewayFrontEndInputLimitTest
{
    private static GatewayTestBackend backend;

    @LocalServerPort
    private int port;

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

    @Test
    public void
    oversizedBodyIsRejected()
    {
        WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:" + port)
            .build()
            .post()
            .uri("/frontendcontroller-user-service/handle-session-input")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(GatewayPublicInputTest.input("Session.New","Login","LoginView") + " ".repeat(128))
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
    }
}

//////////////////////////////////////////////////////////////////////////////
