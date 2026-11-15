//////////////////////////////////////////////////////////////////////////////
// ClientSecurityChainTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.platform;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("CommitStage")
public
class ClientSecurityChainTest
{
    private GenericWebApplicationContext context;
    private MockMvc                      mvc;

    @BeforeEach
    public void
    setUp()
    {
        ClientSecurityChainTestConfiguration.SEEN.set(null);
        context = new GenericWebApplicationContext(new MockServletContext());
        new AnnotatedBeanDefinitionReader(context).register(ClientSecurityChainTestConfiguration.class);
        context.refresh();
        mvc =
            MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain",Filter.class))
                .build();
    }

    @AfterEach
    public void
    tearDown() { context.close(); }

    @Test
    public void
    requiresBearerExceptHealth()
        throws Exception
    {
        mvc
            .perform(
                post("/client-command-service/create-client")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isUnauthorized());

        mvc
            .perform(
                post("/client-command-service/create-client")
                    .header("Authorization","Bearer bad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isUnauthorized());

        int health = mvc.perform(get("/health/status")).andReturn().getResponse().getStatus();

        assertTrue(health != 401 && health != 403,"health must not require a token, was " + health);
    }

    @Test
    public void
    rotateOwnReadsSubjectFromJwt()
        throws Exception
    {
        MvcResult started =
            mvc
                .perform(
                    post("/client-command-service/rotate-own-client-secret")
                        .header("Authorization","Bearer good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(started)).andExpect(status().isOk());

        assertEquals(
            Optional.of(ClientSecurityChainTestConfiguration.SUBJECT),
            ClientSecurityChainTestConfiguration.SEEN.get());
    }

    @Test
    public void
    callerSubjectIsEmptyWithoutJwt()
    {
        assertEquals(Optional.empty(),new JwtCallerSubject().get());
    }
}

//////////////////////////////////////////////////////////////////////////////
