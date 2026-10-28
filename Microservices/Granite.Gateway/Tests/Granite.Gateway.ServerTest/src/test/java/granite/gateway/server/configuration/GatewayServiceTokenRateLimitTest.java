//////////////////////////////////////////////////////////////////////////////
// GatewayServiceTokenRateLimitTest.java
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
@SpringBootTest(
    classes = GatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "granite.gateway.issue-service-token.rate-limit.requests=3",
        "granite.gateway.issue-service-token.rate-limit.window=PT10M"})
public
class GatewayServiceTokenRateLimitTest
{
    private static final String PATH = "/session-command-service/issue-service-token";

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
    issueServiceTokenIsAnonymousAndRateLimitedPerClient()
        throws Exception
    {
        int    before = backend.getReceivedBodies().size();
        String admin  = backend.sign(List.of("ROLE_ADMIN"));

        assertEquals(200,post(PATH));
        assertEquals(200,post(PATH));
        assertEquals(200,post(PATH));
        assertEquals(before + 3,backend.getReceivedBodies().size());
        assertEquals(429,post(PATH));
        assertEquals(429,post(PATH));
        assertEquals(before + 3,backend.getReceivedBodies().size());
        assertEquals(200,post("/session-command-service/login"));
        assertEquals(
            200,
            client
                .post()
                .uri("/user-query-service/find-users-by-key")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(admin))
                .bodyValue("{}")
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value());
    }

    private int
    post(String path)
    {
        return
            client
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"clientId\":\"granite-frontendcontroller\",\"clientSecret\":\"x\"}")
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }
}

//////////////////////////////////////////////////////////////////////////////
