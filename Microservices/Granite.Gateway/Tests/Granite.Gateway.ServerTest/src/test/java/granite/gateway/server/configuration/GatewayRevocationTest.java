//////////////////////////////////////////////////////////////////////////////
// GatewayRevocationTest.java
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@SpringBootTest(
    classes = GatewayTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public
class GatewayRevocationTest
{
    private static final String PATH = "/user-query-service/find-users-by-key";

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
    liveSessionIsAdmittedAndLookupIsCached()
        throws Exception
    {
        String sessionId = backend.newSessionId();
        String token     = backend.sign(sessionId,List.of("ROLE_ADMIN"));

        post(token).expectStatus().isOk();
        post(token).expectStatus().isOk();

        assertEquals(1,Collections.frequency(backend.getSessionLookups(),sessionId));
    }

    @Test
    public void
    revokedSessionIsRejectedEvenThoughTokenIsUnexpired()
        throws Exception
    {
        String sessionId = backend.newSessionId();

        backend.revoke(sessionId);

        post(backend.sign(sessionId,List.of("ROLE_ADMIN"))).expectStatus().isUnauthorized();
        post(backend.sign(sessionId,List.of("ROLE_ADMIN"))).expectStatus().isUnauthorized();
        assertEquals(1,Collections.frequency(backend.getSessionLookups(),sessionId));
    }

    @Test
    public void
    failedLookupFailsClosedAndIsNotCached()
        throws Exception
    {
        String sessionId = backend.newSessionId();
        String token     = backend.sign(sessionId,List.of("ROLE_ADMIN"));

        backend.failLookupsFor(sessionId);

        post(token).expectStatus().isUnauthorized();
        post(token).expectStatus().isUnauthorized();
        assertEquals(2,Collections.frequency(backend.getSessionLookups(),sessionId));
    }

    @Test
    public void
    serviceTokensSkipTheSessionLookup()
        throws Exception
    {
        int before = backend.getSessionLookups().size();

        post(backend.signServiceToken(List.of("SCOPE_internal"))).expectStatus().isOk();
        assertEquals(before,backend.getSessionLookups().size());
    }

    @Test
    public void
    lookupUsesCallersToken()
        throws Exception
    {
        String sessionId = backend.newSessionId();
        String token     = backend.sign(sessionId,List.of("ROLE_ADMIN"));

        post(token).expectStatus().isOk();
        assertTrue(backend.getSessionLookups().contains(sessionId));
        assertTrue(backend.getLookupAuthorizations().contains("Bearer " + token));
    }

    private WebTestClient.ResponseSpec
    post(String token)
    {
        return
            client
                .post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue("{}")
                .exchange();
    }
}

//////////////////////////////////////////////////////////////////////////////
