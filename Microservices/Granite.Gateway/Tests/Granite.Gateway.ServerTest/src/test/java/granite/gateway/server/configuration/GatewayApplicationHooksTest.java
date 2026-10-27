//////////////////////////////////////////////////////////////////////////////
// GatewayApplicationHooksTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = ApplicationHooksGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "granite.gateway.authorization.role-rules.enabled=true")
public
class GatewayApplicationHooksTest
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
    applicationRuleGrantsApplicationRole()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_PromotionsReviewer"));

        assertNotEquals(403,call(HttpMethod.POST,"/frontendcontroller-user-service/get-promotions-output",token));
    }

    @Test
    public void
    applicationRoleDeniedElsewhere()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_PromotionsReviewer"));

        assertEquals(403,call(HttpMethod.POST,"/frontendcontroller-user-service/handle-users-input",token));
    }

    @Test
    public void
    adminCatchAllStillApplies()
        throws Exception
    {
        assertNotEquals(403,call(HttpMethod.GET,"/unlisted-service/anything",backend.sign(List.of("ROLE_Admin"))));
        assertEquals(
            403,
            call(HttpMethod.GET,"/unlisted-service/anything",backend.sign(List.of("ROLE_PromotionsReviewer"))));
    }

    @Test
    public void
    applicationRulesCannotReopenDeniedPaths()
        throws Exception
    {
        assertEquals(
            403,
            call(
                HttpMethod.POST,
                "/session-command-service/resolve-session",
                backend.sign(List.of("ROLE_PromotionsReviewer"))));
    }

    @Test
    public void
    baseConfigurationDoesNotKnowAuditor()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_Auditor"));

        assertEquals(403,call(HttpMethod.POST,"/frontendcontroller-user-service/get-users-output",token));
        assertEquals(403,call(HttpMethod.POST,"/user-query-service/find-users-by-key",token));
    }

    @Test
    public void
    applicationRouteIsRegistered()
    {
        List<String> ids =
            routeLocator
                .getRoutes()
                .map(route -> route.getId())
                .collectList()
                .block();

        assertTrue(ids.contains("promotion-query-route"),ids.toString());
        assertTrue(ids.contains("frontendcontroller-user-route"),ids.toString());
    }

    private int
    call(HttpMethod method,String path,String token)
    {
        return
            client
                .method(method)
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue("{}")
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }
}

//////////////////////////////////////////////////////////////////////////////
