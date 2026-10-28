//////////////////////////////////////////////////////////////////////////////
// FrontEndControllerSecurityTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("CommitStage")
public
class FrontEndControllerSecurityTest
{
    private static final String PATH = "/frontendcontroller-test-service";

    private AnnotationConfigWebApplicationContext context;
    private MockMvc                               mvc;

    @BeforeEach
    public void
    setUp()
    {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context
            .getEnvironment()
            .getPropertySources()
            .addFirst(
                new MapPropertySource(
                    "test",
                    Map.of(
                        "granite.frontendcontroller.service-path",PATH,
                        "granite.security.jwt.jwk-set-uri","https://session.invalid/.well-known/jwks.json")));
        context.register(MvcTestSupport.class,SecuredMockClientsConfiguration.class);
        context.refresh();

        mvc =
            MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain",Filter.class))
                .build();
    }

    @AfterEach
    public void
    tearDown()
    {
        SecurityContextHolder.clearContext();
        context.close();
    }

    private MockHttpServletRequestBuilder
    loginMethods()
    {
        return
            post(PATH + "/get-session-output")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"source\":\"Session.New\",\"destination\":\"LoginView\"}");
    }

    private MockHttpServletRequestBuilder
    allUsers()
    {
        return
            post(PATH + "/get-users-output")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"source\":\"User.All\",\"destination\":\"UsersEditableView\"}");
    }

    private SessionRequestContext
    callerOf(MvcResult result)
    {
        return (SessionRequestContext)result.getRequest().getAttribute(SessionRequestContext.ATTRIBUTE);
    }

    @Test
    public void
    anonymousSessionNewRequestPasses()
        throws Exception
    {
        MvcResult started = mvc.perform(loginMethods()).andExpect(request().asyncStarted()).andReturn();

        mvc.perform(asyncDispatch(started)).andExpect(status().isOk());
        assertTrue(callerOf(started).getCallerUserId().isEmpty());
    }

    @Test
    public void
    forgedBearerIsRejected()
        throws Exception
    {
        mvc
            .perform(loginMethods().header("Authorization","Bearer " + TestJwks.forgedToken(11,7,Duration.ofMinutes(5))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void
    expiredBearerIsRejected()
        throws Exception
    {
        mvc
            .perform(loginMethods().header("Authorization","Bearer " + TestJwks.trustedToken(11,7,Duration.ofMinutes(-5))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void
    unsignedBearerIsRejectedNotTrusted()
        throws Exception
    {
        String claims = "{\"jti\":\"11\",\"sub\":\"7\"}";
        String unsigned =
            "Bearer eyJhbGciOiJub25lIn0."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(claims.getBytes(StandardCharsets.UTF_8))
                + ".";

        MvcResult result =
            mvc.perform(allUsers().header("Authorization",unsigned)).andExpect(status().isUnauthorized()).andReturn();

        assertNull(callerOf(result));
    }

    @Test
    public void
    callerComesFromTheVerifiedToken()
        throws Exception
    {
        MvcResult started =
            mvc
                .perform(allUsers().header("Authorization","Bearer " + TestJwks.trustedToken(11,7,Duration.ofMinutes(5))))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertEquals(7L,callerOf(started).getCallerUserId().orElseThrow());
        assertEquals(11L,callerOf(started).getSessionId().orElseThrow());
        mvc.perform(asyncDispatch(started)).andExpect(status().isOk());
    }
}

//////////////////////////////////////////////////////////////////////////////
