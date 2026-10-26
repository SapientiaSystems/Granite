//////////////////////////////////////////////////////////////////////////////
// GatewayRoleRulesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Tag("CommitStage")
@SpringBootTest(
    classes = RoleRulesGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "granite.gateway.authorization.role-rules.enabled=true")
public
class GatewayRoleRulesTest
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
    adminMatrix()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_ADMIN"));

        assertAllowed(HttpMethod.POST,"/frontendcontroller-user-service/get-users-output",token);
        assertAllowed(HttpMethod.POST,"/frontendcontroller-admin-service/get-users-output",token);
        assertAllowed(HttpMethod.POST,"/frontendcontroller-user-service/handle-users-input",token);
        assertAllowed(HttpMethod.POST,"/frontendcontroller-admin-service/handle-users-input",token);
        assertAllowed(HttpMethod.POST,"/user-query-service/find-users-by-key",token);
        assertAllowed(HttpMethod.POST,"/user-command-service/create-user",token);
        assertAllowed(HttpMethod.POST,"/session-command-service/logout",token);
        assertAllowed(HttpMethod.POST,"/session-command-service/logout-all-for-user",token);
        assertAllowed(HttpMethod.POST,"/session-query-service/find-sessions-by-key",token);
        assertAllowed(HttpMethod.POST,"/outbox-service/anything",token);
    }

    @Test
    public void
    auditorMatrix()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_AUDITOR"));

        assertAllowed(HttpMethod.POST,"/frontendcontroller-user-service/get-users-output",token);
        assertAllowed(HttpMethod.POST,"/frontendcontroller-admin-service/get-users-output",token);
        assertAllowed(HttpMethod.POST,"/user-query-service/find-users-by-key",token);
        assertAllowed(HttpMethod.POST,"/session-command-service/logout",token);
        assertAllowed(HttpMethod.POST,"/session-query-service/find-sessions-by-key",token);
        assertForbidden(HttpMethod.POST,"/frontendcontroller-user-service/handle-users-input",token);
        assertForbidden(HttpMethod.POST,"/frontendcontroller-admin-service/handle-users-input",token);
        assertForbidden(HttpMethod.POST,"/user-command-service/create-user",token);
        assertForbidden(HttpMethod.POST,"/session-command-service/logout-all-for-user",token);
        assertForbidden(HttpMethod.POST,"/outbox-service/anything",token);
    }

    @Test
    public void
    retiredSharedPathsFallToCatchAll()
        throws Exception
    {
        String admin   = backend.sign(List.of("ROLE_ADMIN"));
        String auditor = backend.sign(List.of("ROLE_AUDITOR"));

        assertAllowed(HttpMethod.POST,"/frontendcontroller-user-service/handle-input",admin);
        assertForbidden(HttpMethod.POST,"/frontendcontroller-user-service/handle-input",auditor);
        assertForbidden(HttpMethod.POST,"/frontendcontroller-user-service/get-output",auditor);
    }

    @Test
    public void
    roleUserIsForbiddenEverywhere()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_USER"));

        assertForbidden(HttpMethod.POST,"/frontendcontroller-user-service/get-users-output",token);
        assertForbidden(HttpMethod.POST,"/user-query-service/find-users-by-key",token);
    }

    @Test
    public void
    serviceTokenKeepsAccess()
        throws Exception
    {
        String token = backend.signServiceToken(List.of("SCOPE_internal"));

        assertAllowed(HttpMethod.POST,"/user-command-service/create-user",token);
        assertAllowed(HttpMethod.POST,"/session-query-service/find-sessions-by-key",token);
    }

    @Test
    public void
    unlistedPathsAreAdminOnly()
        throws Exception
    {
        assertForbidden(HttpMethod.GET,"/unlisted-service/anything",backend.sign(List.of("ROLE_AUDITOR")));
        assertAllowed(HttpMethod.GET,"/unlisted-service/anything",backend.sign(List.of("ROLE_ADMIN")));
    }

    @Test
    public void
    publicPathsStayOpen()
    {
        assertAllowed(HttpMethod.POST,"/session-command-service/login",null);
        assertAllowed(HttpMethod.GET,"/health/liveness",null);
    }

    @Test
    public void
    missingTokenIsUnauthorized()
    {
        assertEquals(401,call(HttpMethod.POST,"/frontendcontroller-user-service/get-users-output",null));
    }

    private void
    assertAllowed(HttpMethod method,String path,String token)
    {
        int status = call(method,path,token);

        assertNotEquals(401,status,method + " " + path);
        assertNotEquals(403,status,method + " " + path);
    }

    private void
    assertForbidden(HttpMethod method,String path,String token)
    {
        assertEquals(403,call(method,path,token),method + " " + path);
    }

    private int
    call(HttpMethod method,String path,String token)
    {
        return
            client
                .method(method)
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
