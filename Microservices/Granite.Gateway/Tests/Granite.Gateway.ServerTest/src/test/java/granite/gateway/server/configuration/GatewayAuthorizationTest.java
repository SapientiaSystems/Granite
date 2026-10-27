//////////////////////////////////////////////////////////////////////////////
// GatewayAuthorizationTest.java
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = AuthorizingGatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GatewayAuthorizationTest
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
        backend.getReceivedAuthorizations().clear();
    }

    @Test
    public void
    missingTokenIsRejectedWithBearerChallenge()
    {
        post("/user-command-service/create-user",null)
            .expectStatus().isUnauthorized()
            .expectHeader().value("WWW-Authenticate",value -> assertTrue(value.startsWith("Bearer")));
        assertTrue(backend.getReceivedAuthorizations().isEmpty());
    }

    @Test
    public void
    tokenFromUntrustedKeyIsRejected()
        throws Exception
    {
        post("/user-command-service/create-user",backend.signWithUntrustedKey(List.of("ROLE_Admin")))
            .expectStatus().isUnauthorized();
        assertTrue(backend.getReceivedAuthorizations().isEmpty());
    }

    @Test
    public void
    applicationRuleForbidsCallerWithoutRequiredAuthority()
        throws Exception
    {
        post("/user-command-service/create-user",backend.sign(List.of("ROLE_User")))
            .expectStatus().isForbidden();
        assertTrue(backend.getReceivedAuthorizations().isEmpty());
    }

    @Test
    public void
    applicationRuleAdmitsCallerWithRequiredAuthorityAndRelaysToken()
        throws Exception
    {
        String token = backend.sign(List.of("ROLE_Admin"));

        post("/user-command-service/create-user",token)
            .expectStatus().isOk();
        assertEquals(List.of("Bearer " + token),backend.getReceivedAuthorizations());
    }

    @Test
    public void
    applicationRuleAcceptsAnyListedAuthority()
        throws Exception
    {
        post("/session-query-service/find-sessions-by-key",backend.sign(List.of("SCOPE_internal")))
            .expectStatus().isOk();
        post("/session-query-service/find-sessions-by-key",backend.sign(List.of("ROLE_User")))
            .expectStatus().isForbidden();
    }

    @Test
    public void
    coreReaderRulesApplyWithoutRoleRulesFlag()
        throws Exception
    {
        post("/user-query-service/find-users-by-key",backend.sign(List.of("ROLE_User")))
            .expectStatus().isForbidden();
        post("/user-query-service/find-users-by-key",backend.sign(List.of("ROLE_Admin")))
            .expectStatus().isOk();
        post("/user-query-service/find-users-by-key",null)
            .expectStatus().isUnauthorized();
    }

    @Test
    public void
    loginIsPublic()
    {
        post("/session-command-service/login",null)
            .expectStatus().isOk();
    }

    private WebTestClient.ResponseSpec
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
                .exchange();
    }
}

//////////////////////////////////////////////////////////////////////////////
