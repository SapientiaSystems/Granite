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
    properties = {
        "granite.security.jwt.issuer=granite-session",
        "granite.security.jwt.audience=granite-services"},
    classes = GatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GatewayProductionRoutesTest
{
    private static final List<String> INTERNAL_PATHS =
        List.of(
            "/user-query-service/find-users-by-key",
            "/user-command-service/create-user",
            "/session-query-service/get-sessions",
            "/session-command-service/logout",
            "/session-command-service/close-session",
            "/feedback-query-service/find-all-feedback",
            "/feedback-command-service/create-feedback",
            "/outbox-service/get-entries");

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
    productionExposesAllGraniteRoutes()
    {
        List<Route> routes = routeLocator.getRoutes().collectList().block();

        assertEquals(
            Set.of(
                "frontendcontroller-user-route",
                "frontendcontroller-admin-route",
                "user-command-route",
                "user-query-route",
                "session-command-route",
                "session-query-route",
                "feedback-command-route",
                "feedback-query-route",
                "outbox-route"),
            routes.stream().map(Route::getId).collect(Collectors.toSet()));
    }

    @Test
    public void
    browserSessionsAreForbiddenOnInternalRoutes()
        throws Exception
    {
        for (String role : List.of("ROLE_ADMIN","ROLE_USER","ROLE_AUDITOR"))
        {
            String token  = backend.sign(List.of(role));
            int    before = backend.getReceivedAuthorizations().size();

            for (String path : INTERNAL_PATHS)
                assertEquals(403,post(path,token),role + " " + path);

            assertEquals(before,backend.getReceivedAuthorizations().size(),role);
        }
    }

    @Test
    public void
    internalRoutesRequireAToken()
    {
        for (String path : INTERNAL_PATHS)
            assertEquals(401,post(path,null),path);
    }

    @Test
    public void
    serviceTokensReachInternalRoutes()
        throws Exception
    {
        for (String token :
                List.of(
                    backend.signClientServiceToken("granite-frontendcontroller"),
                    backend.signServiceToken(List.of("SCOPE_internal"))))
        {
            for (String path : INTERNAL_PATHS)
            {
                int before = backend.getReceivedAuthorizations().size();

                assertEquals(200,post(path,token),path);
                assertEquals(before + 1,backend.getReceivedAuthorizations().size(),path);
            }
        }
    }

    @Test
    public void
    oktaStepsAreForbiddenToBrowserSessionsAndOpenToServiceTokens()
        throws Exception
    {
        String admin   = backend.sign(List.of("ROLE_ADMIN"));
        String service = backend.signClientServiceToken("granite-frontendcontroller");

        for (String path : List.of("/session-command-service/start-okta-login","/session-command-service/complete-okta-login"))
        {
            assertEquals(401,post(path,null),path);
            assertEquals(403,post(path,admin),path);
            assertEquals(200,post(path,service),path);
        }
    }

    @Test
    public void
    resolveSessionIsDeniedToEveryCaller()
        throws Exception
    {
        String path = "/session-command-service/resolve-session";
        int    seen = backend.getResolveCalls().size();

        assertEquals(401,post(path,null));
        assertEquals(403,post(path,backend.sign(List.of("ROLE_ADMIN"))));
        assertEquals(403,post(path,backend.signClientServiceToken("granite-frontendcontroller")));
        assertEquals(403,post(path,backend.signServiceToken(List.of("SCOPE_internal"))));
        assertEquals(seen,backend.getResolveCalls().size());
    }

    @Test
    public void
    serviceTokenIssuanceStaysPublic()
    {
        assertEquals(200,post("/session-command-service/issue-service-token",null));
    }

    @Test
    public void
    loginAndRefreshRequireTheInternalScope()
        throws Exception
    {
        String browser = backend.sign(List.of("ROLE_ADMIN","ROLE_USER"));
        String service = backend.signClientServiceToken("granite-frontendcontroller");

        for (String path : List.of("/session-command-service/login","/session-command-service/refresh"))
        {
            int before = backend.getReceivedAuthorizations().size();

            assertEquals(401,post(path,null),path);
            assertEquals(403,post(path,browser),path);
            assertEquals(before,backend.getReceivedAuthorizations().size(),path);
            assertEquals(200,post(path,service),path);
            assertEquals(before + 1,backend.getReceivedAuthorizations().size(),path);
        }
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
