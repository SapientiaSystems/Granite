//////////////////////////////////////////////////////////////////////////////
// SessionCommandHostSmokeTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.commandhost.main;

import granite.session.server.application.IJwtTokenService;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    classes = SessionCommandHost.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "granite.deploy-env=smoke")
@ActiveProfiles({"smoke","dev"})
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
}

//////////////////////////////////////////////////////////////////////////////
