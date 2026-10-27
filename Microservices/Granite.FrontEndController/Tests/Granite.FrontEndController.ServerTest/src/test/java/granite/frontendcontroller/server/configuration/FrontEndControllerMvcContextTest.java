//////////////////////////////////////////////////////////////////////////////
// FrontEndControllerMvcContextTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("CommitStage")
public
class FrontEndControllerMvcContextTest
{
    private static final String PATH = "/frontendcontroller-test-service";

    private static final List<String> GRANITE_PATHS =
        List.of("/get-session-output","/handle-session-input","/get-users-output","/handle-users-input");

    private AnnotationConfigWebApplicationContext context;

    @AfterEach
    public void
    tearDown()
    {
        if (context != null)
            context.close();
    }

    @Test
    public void
    graniteConfigurationExposesGraniteEndpoints()
        throws Exception
    {
        MockMvc mvc = start(GraniteMockClientsConfiguration.class);

        assertPaths(GRANITE_PATHS);
        assertRoles(mvc);
    }

    @Test
    public void
    extendedConfigurationExposesInheritedAndOwnEndpoints()
        throws Exception
    {
        MockMvc      mvc   = start(ExtendedFrontEndControllerConfiguration.class);
        List<String> paths = new ArrayList<>(GRANITE_PATHS);

        paths.add("/get-echo-output");
        assertPaths(paths);
        assertRoles(mvc);

        MvcResult started =
            mvc
                .perform(
                    post(PATH + "/get-echo-output")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"Session.Current\",\"destination\":\"SessionView\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc
            .perform(asyncDispatch(started))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.output.userName").value("echo"));
    }

    private MockMvc
    start(Class<?> configuration)
    {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context
            .getEnvironment()
            .getPropertySources()
            .addFirst(new MapPropertySource("test",Map.of("granite.frontendcontroller.service-path",PATH)));
        context.register(MvcTestSupport.class,configuration);
        context.refresh();

        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    private void
    assertPaths(List<String> expected)
    {
        RequestMappingHandlerMapping mapping = context.getBean(RequestMappingHandlerMapping.class);
        List<String>                 paths   = new ArrayList<>();

        for (RequestMappingInfo info : mapping.getHandlerMethods().keySet())
            if (info.getPathPatternsCondition() != null)
                paths.addAll(info.getPathPatternsCondition().getPatternValues());

        for (String path : expected)
            assertTrue(paths.contains(PATH + path),"missing " + path + " in " + paths);
    }

    private void
    assertRoles(MockMvc mvc)
        throws Exception
    {
        MvcResult started =
            mvc
                .perform(
                    post(PATH + "/get-users-output")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"User.All\",\"destination\":\"UsersEditableView\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc
            .perform(asyncDispatch(started))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.output.roles[0].name").value("ROLE_Admin"));
    }
}

//////////////////////////////////////////////////////////////////////////////
