//////////////////////////////////////////////////////////////////////////////
// SessionQueryHostSmokeTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.queryhost.main;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    classes = SessionQueryHost.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "DEPLOY_ENV=smoke")
@ActiveProfiles({"smoke"})
public
class SessionQueryHostSmokeTest
{
    @Autowired
    private TestRestTemplate rest;

    @Test
    public void
    queryEndpointIsMappedAndNotSecurityBlocked()
    {
        HttpStatusCode status =
            rest.postForEntity(
                    "/session-query-service/find-sessions-by-key",
                    "{}",
                    String.class)
                .getStatusCode();

        assertTrue(
            status.value() != 404 && status.value() != 401 && status.value() != 403,
            "unexpected status " + status);
    }
}

//////////////////////////////////////////////////////////////////////////////
