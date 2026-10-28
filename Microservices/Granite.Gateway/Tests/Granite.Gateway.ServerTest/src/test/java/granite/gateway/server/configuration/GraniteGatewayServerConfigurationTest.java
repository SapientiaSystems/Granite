//////////////////////////////////////////////////////////////////////////////
// GraniteGatewayServerConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import granite.gateway.server.health.GatewayHealthCheckController;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = GatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GraniteGatewayServerConfigurationTest
{
    private static final List<String> SWAGGER_PATHS = List.of("/*/swagger-ui/**","/*/swagger-ui.html","/*/v3/api-docs/**");

    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private WebTestClient client;

    @Test
    public void
    testGraniteRoutesAreRegisteredWithConfiguredHosts()
    {
        Map<String,URI> routes = getRoutes();

        assertEquals(7,routes.size());
        assertEquals(URI.create("http://frontendcontroller-user.test:8081"),routes.get("frontendcontroller-user-route"));
        assertEquals(URI.create("http://frontendcontroller-admin.test:8082"),routes.get("frontendcontroller-admin-route"));
        assertEquals(URI.create("http://user-command.test:8083"),routes.get("user-command-route"));
        assertEquals(URI.create("http://user-query.test:8084"),routes.get("user-query-route"));
        assertEquals(URI.create("http://session-command.test:8088"),routes.get("session-command-route"));
        assertEquals(URI.create("http://session-query.test:8089"),routes.get("session-query-route"));
        assertEquals(URI.create("http://outbox.test:8085"),routes.get("outbox-route"));
    }

    @Test
    public void
    testSecurityChainAndHealthControllerAreDefined()
    {
        assertTrue(context.getBeanNamesForType(SecurityWebFilterChain.class).length > 0);
        assertTrue(context.getBeanNamesForType(GatewayHealthCheckController.class).length > 0);
    }

    @Test
    public void
    testLivenessIsReachableWithoutAuthentication()
    {
        client
            .get()
            .uri("/health/liveness")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    public void
    testSwaggerPathsArePublicOutsideProduction()
    {
        for (String profile : new String[] {"local","development","test"})
        {
            List<String> paths = new GraniteGatewayServerConfiguration().getPublicPathPatterns(environment(profile));

            assertTrue(paths.containsAll(SWAGGER_PATHS),profile);
        }
    }

    @Test
    public void
    testSwaggerPathsAreNotPublicInProduction()
    {
        List<String> paths = new GraniteGatewayServerConfiguration().getPublicPathPatterns(environment("production"));

        for (String path : SWAGGER_PATHS)
            assertFalse(paths.contains(path),path);
    }

    @Test
    public void
    testIssueServiceTokenIsPublicInEveryProfile()
    {
        for (String profile : new String[] {"local","development","test","production"})
            assertTrue(
                new GraniteGatewayServerConfiguration()
                    .getPublicPathPatterns(environment(profile))
                    .contains("/session-command-service/issue-service-token"),
                profile);
    }

    private Map<String,URI>
    getRoutes()
    {
        List<Route> routes = routeLocator.getRoutes().collectList().block();

        return
            routes
                .stream()
                .collect(Collectors.toMap(Route::getId,Route::getUri));
    }

    private static MockEnvironment
    environment(String profile)
    {
        MockEnvironment environment = new MockEnvironment();

        environment.setActiveProfiles(profile);
        return environment;
    }
}

//////////////////////////////////////////////////////////////////////////////
