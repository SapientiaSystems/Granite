//////////////////////////////////////////////////////////////////////////////
// GatewayProductionRoutesTest.java
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
    classes = GatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GatewayProductionRoutesTest
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
    productionExposesOnlyFrontEndControllerRoutes()
    {
        List<Route> routes = routeLocator.getRoutes().collectList().block();

        assertEquals(
            Set.of("frontendcontroller-user-route","frontendcontroller-admin-route"),
            routes.stream().map(Route::getId).collect(Collectors.toSet()));
    }

    @Test
    public void
    productionDoesNotRouteDirectServicePaths()
        throws Exception
    {
        int    before = backend.getReceivedAuthorizations().size();
        String token  = backend.sign(List.of("ROLE_Admin"));

        client
            .post()
            .uri("/user-query-service/find-users-by-key")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue("{}")
            .exchange()
            .expectStatus()
            .isNotFound();
        assertEquals(before,backend.getReceivedAuthorizations().size());
    }
}

//////////////////////////////////////////////////////////////////////////////
