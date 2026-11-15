//////////////////////////////////////////////////////////////////////////////
// ExtendedGatewayServerConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = ExtendedGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class ExtendedGatewayServerConfigurationTest
{
    @Autowired
    private RouteLocator routeLocator;

    @Test
    public void
    testConfigureRoutesAddsDownstreamRoute()
    {
        assertEquals(
            URI.create("http://order-command.test:9001"),
            getRoutes().get("order-command-route"));
    }

    @Test
    public void
    testConfigureRoutesRetainsGraniteRoutes()
    {
        Map<String,URI> routes = getRoutes();

        assertEquals(18,routes.size());
        assertTrue(routes.containsKey("user-command-route"));
        assertTrue(routes.containsKey("session-query-route"));
        assertTrue(routes.containsKey("outbox-route"));
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
}

//////////////////////////////////////////////////////////////////////////////
