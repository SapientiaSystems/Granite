//////////////////////////////////////////////////////////////////////////////
// GatewayClientAdminApplicationsTest.java
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
    classes = ClientGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "granite.client.admin-applications=poplar-clients")
public
class GatewayClientAdminApplicationsTest
{
    private static final String FEC = "/frontendcontroller-user-service/";

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
    configuredClientAdminApplicationIsConfined()
        throws Exception
    {
        List<String> authorities = List.of("ROLE_ADMIN","ROLE_CLIENT_ADMIN");
        String       confined    = backend.sign(authorities,"poplar-clients");

        assertEquals(200,post(FEC + "get-owned-clients-output",confined));
        assertEquals(403,post(FEC + "handle-users-input",confined));
        assertEquals(403,post("/user-query-service/x",confined));
    }

    @Test
    public void
    applicationsOutsideTheListAreNotConfined()
        throws Exception
    {
        List<String> authorities = List.of("ROLE_ADMIN","ROLE_CLIENT_ADMIN");

        assertEquals(200,post(FEC + "handle-users-input",backend.sign(authorities,"client")));
        assertEquals(200,post(FEC + "handle-users-input",backend.sign(authorities)));
    }

    private int
    post(String path,String token)
    {
        return
            client
                .post()
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
