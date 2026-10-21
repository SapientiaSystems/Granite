//////////////////////////////////////////////////////////////////////////////
// SessionCommandHostSmokeTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.commandhost.main;

import granite.session.server.application.IJwtTokenService;
import granite.session.server.domain.Session;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    classes = SessionCommandHost.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "DEPLOY_ENV=local")
@ActiveProfiles({"local"})
public
class SessionCommandHostSmokeTest
{
    @Autowired
    private IJwtTokenService jwtTokenService;

    @Autowired
    private IUserCommandServiceClient userCommandServiceClient;

    @Autowired
    private IUserQueryServiceClient userQueryServiceClient;

    @Autowired
    private TestRestTemplate rest;

    @Test
    public void
    contextLoadsWithHostLevelBeans()
    {
        assertNotNull(jwtTokenService);
        assertNotNull(userCommandServiceClient);
        assertNotNull(userQueryServiceClient);
    }

    @Test
    public void
    commandEndpointIsMappedAndNotSecurityBlocked()
    {
        HttpStatusCode status =
            rest.postForEntity(
                    "/session-command-service/login",
                    "{}",
                    String.class)
                .getStatusCode();

        assertTrue(
            status.value() != 404 && status.value() != 401 && status.value() != 403,
            "unexpected status " + status);
    }

    @Test
    public void
    protectedEndpointRejectsMissingToken()
    {
        assertEquals(401,logout(null).getStatusCode().value());
    }

    @Test
    public void
    protectedEndpointAcceptsSessionIssuedToken()
    {
        String token =
            jwtTokenService.mint(
                new Session()
                    .setSessionId(999999L)
                    .setUserId(1001L)
                    .setDeviceId("smoke-device")
                    .setExpires(Instant.now().plusSeconds(300)));
        ResponseEntity<String> response = logout(token);

        assertEquals(200,response.getStatusCode().value(),response.getBody());
    }

    @Test
    public void
    jwkSetEndpointPublishesSigningKey()
    {
        ResponseEntity<String> response =
            rest.getForEntity("/.well-known/jwks.json",String.class);

        assertEquals(200,response.getStatusCode().value());
        assertTrue(response.getBody().contains("\"kid\""),response.getBody());
        assertTrue(response.getBody().contains("\"RS256\""),response.getBody());
        assertFalse(response.getBody().contains("\"d\""),response.getBody());
    }

    private ResponseEntity<String>
    logout(String token)
    {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        if (token != null)
            headers.setBearerAuth(token);

        return
            rest.postForEntity(
                "/session-command-service/logout",
                new HttpEntity<>("{\"sessionId\":999999}",headers),
                String.class);
    }
}

//////////////////////////////////////////////////////////////////////////////
