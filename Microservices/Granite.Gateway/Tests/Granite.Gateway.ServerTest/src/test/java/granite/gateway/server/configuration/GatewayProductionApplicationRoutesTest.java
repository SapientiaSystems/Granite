//////////////////////////////////////////////////////////////////////////////
// GatewayProductionApplicationRoutesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
@ActiveProfiles("production")
@SpringBootTest(
    properties = {
        "granite.security.jwt.issuer=granite-session",
        "granite.security.jwt.audience=granite-services"},
    classes = ApplicationHooksGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GatewayProductionApplicationRoutesTest
{
    private static GatewayTestBackend backend;

    @LocalServerPort
    private int port;

    @Autowired
    private RouteLocator routeLocator;

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
    productionRegistersApplicationRoutes()
    {
        Set<String> ids = routeLocator.getRoutes().map(Route::getId).collect(Collectors.toSet()).block();

        assertEquals(
            Set.of(
                "frontendcontroller-user-route",
                "frontendcontroller-admin-route",
                "user-command-route",
                "user-query-route",
                "session-command-route",
                "session-query-route",
                "feedback-command-route",
                "survey-command-route",
                "agent-command-route",
                "content-command-route",
                "feedback-query-route",
                "survey-query-route",
                "agent-query-route",
                "content-query-route",
                "outbox-route",
                "client-command-route",
                "client-query-route",
                "promotion-query-route"),
            ids);
    }

    @Test
    public void
    applicationRulesCannotOpenInternalRoutesInProduction()
        throws Exception
    {
        String admin = backend.sign(List.of("ROLE_ADMIN"));

        assertEquals(401,post("/user-command-service/create-user",null));
        assertEquals(403,post("/user-command-service/create-user",admin));
        assertEquals(403,post("/session-command-service/resolve-session",admin));
        assertEquals(
            200,
            post("/user-command-service/create-user",backend.signClientServiceToken("granite-frontendcontroller")));
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
