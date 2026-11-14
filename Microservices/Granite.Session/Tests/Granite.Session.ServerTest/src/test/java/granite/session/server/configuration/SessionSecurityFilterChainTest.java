//////////////////////////////////////////////////////////////////////////////
// SessionSecurityFilterChainTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import granite.session.server.application.IJwtTokenService;
import granite.session.server.platform.PlatformContextTestConfiguration;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionSecurityFilterChainTest
{
    private static final String SERVICE_PATH = "/session-command-service/logout";

    private static ConfigurableApplicationContext context;
    private static Filter                         securityFilterChain;
    private static IJwtTokenService               tokens;

    @BeforeAll
    public static void
    initialize()
    {
        AnnotationConfigApplicationContext created = new AnnotationConfigApplicationContext();

        created.getEnvironment().setActiveProfiles("local");
        created.register(PlatformContextTestConfiguration.class);
        created.refresh();

        context             = created;
        securityFilterChain = context.getBean("springSecurityFilterChain",Filter.class);
        tokens              = context.getBean(IJwtTokenService.class);
    }

    @AfterAll
    public static void
    terminate()
    {
        context.close();
    }

    @Test
    public void
    servicePathRejectsMissingToken()
        throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = run("POST",SERVICE_PATH,null,response);

        assertEquals(401,response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    public void
    servicePathRejectsGarbageToken()
        throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();

        run("POST",SERVICE_PATH,"not-a-jwt",response);
        assertEquals(401,response.getStatus());
    }

    @Test
    public void
    oktaLoginStepsRequireToken()
        throws Exception
    {
        for (String path : List.of(
                "/session-command-service/start-okta-login",
                "/session-command-service/complete-okta-login"))
        {
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain         chain    = run("POST",path,null,response);

            assertEquals(401,response.getStatus(),path);
            assertNull(chain.getRequest(),path);
        }
    }

    @Test
    public void
    oktaLoginStepsAcceptServiceToken()
        throws Exception
    {
        String token = tokens.mintServiceToken("granite-frontendcontroller",List.of("SCOPE_internal"),Duration.ofMinutes(5));

        for (String path : List.of(
                "/session-command-service/start-okta-login",
                "/session-command-service/complete-okta-login"))
        {
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain         chain    = run("POST",path,token,response);

            assertNotNull(chain.getRequest(),path);
        }
    }

    @Test
    public void
    tokenlessPathsAreOpen()
        throws Exception
    {
        for (String path : List.of(
                "/session-command-service/login",
                "/session-command-service/refresh",
                "/session-command-service/resolve-session",
                "/session-command-service/issue-service-token",
                "/.well-known/jwks.json",
                "/health/check"))
        {
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain         chain    = run("POST",path,null,response);

            assertNotNull(chain.getRequest(),path);
            assertTrue(response.getStatus() != 401 && response.getStatus() != 403,path);
        }
    }

    @Test
    public void
    tokenEndpointIsTokenless()
        throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = run("POST","/session-command-service/oauth2/token",null,response);

        assertNotNull(chain.getRequest());
        assertTrue(response.getStatus() != 401 && response.getStatus() != 403);
    }

    @Test
    public void
    validTokenWithoutRoleAuthoritiesReachesServicePath()
        throws Exception
    {
        String                  token    = tokens.mintServiceToken("service:test",List.of(),Duration.ofMinutes(5));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = run("POST",SERVICE_PATH,token,response);

        assertNotNull(chain.getRequest());
        assertTrue(response.getStatus() != 401 && response.getStatus() != 403);
    }

    private static MockFilterChain
    run(String method,String path,String token,MockHttpServletResponse response)
        throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest(method,path);

        request.setServletPath(path);

        if (token != null)
            request.addHeader("Authorization","Bearer " + token);

        MockFilterChain chain = new MockFilterChain(new HttpServlet() {});

        securityFilterChain.doFilter(request,response,chain);
        SecurityContextHolder.clearContext();
        return chain;
    }
}

//////////////////////////////////////////////////////////////////////////////
