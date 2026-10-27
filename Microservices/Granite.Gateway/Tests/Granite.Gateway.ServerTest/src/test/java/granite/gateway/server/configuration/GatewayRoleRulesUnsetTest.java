//////////////////////////////////////////////////////////////////////////////
// GatewayRoleRulesUnsetTest.java
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
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GatewayRoleRulesUnsetTest
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
    roleUserCannotAdministerUsers()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_User"));

        assertForbidden(HttpMethod.POST,"/frontendcontroller-user-service/handle-users-input",token);
        assertForbidden(HttpMethod.POST,"/frontendcontroller-admin-service/handle-users-input",token);
        assertForbidden(HttpMethod.POST,"/user-command-service/create-user",token);
    }

    @Test
    public void
    unmatchedPathsAreAdminOnly()
        throws Exception
    {
        assertForbidden(HttpMethod.GET,"/unlisted-service/anything",backend.sign(List.of("ROLE_User")));
        assertAllowed(HttpMethod.GET,"/unlisted-service/anything",backend.sign(List.of("ROLE_Admin")));
    }

    @Test
    public void
    auditorIsNotGrantedReaderAccess()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_Auditor"));

        assertForbidden(HttpMethod.POST,"/frontendcontroller-user-service/get-users-output",token);
        assertForbidden(HttpMethod.POST,"/user-query-service/find-users-by-key",token);
    }

    @Test
    public void
    adminKeepsReaderAndAdministrationAccess()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_Admin"));

        assertAllowed(HttpMethod.POST,"/frontendcontroller-user-service/get-users-output",token);
        assertAllowed(HttpMethod.POST,"/frontendcontroller-user-service/handle-users-input",token);
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
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue("{}")
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }
}

//////////////////////////////////////////////////////////////////////////////
