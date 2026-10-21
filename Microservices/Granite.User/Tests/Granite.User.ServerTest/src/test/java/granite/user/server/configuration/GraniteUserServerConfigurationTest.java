//////////////////////////////////////////////////////////////////////////////
// GraniteUserServerConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.configuration;

import granite.user.server.application.TestConfiguration;
import granite.user.server.domain.User;
import granite.user.server.platform.MockUserEventSender;
import granite.user.server.platform.OutboxEventUserEventSender;
import granite.user.service.event.IUserEventSender;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteUserServerConfigurationTest
{
    private static final List<String> SWAGGER_PATHS = List.of("/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**");

    private static ConfigurableApplicationContext defaultContext;
    private static ConfigurableApplicationContext extendedContext;

    @BeforeAll
    public static void
    initialize()
    {
        defaultContext  = new AnnotationConfigApplicationContext(TestConfiguration.class);
        extendedContext =
            new AnnotationConfigApplicationContext(
                ExtendedUserCommandServerConfiguration.class);
    }

    @AfterAll
    public static void
    terminate()
    {
        defaultContext.close();
        extendedContext.close();
    }

    @BeforeEach
    public void
    setUp()
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    testDefaultEventSenderIsOutboxSender()
    {
        assertInstanceOf(
            OutboxEventUserEventSender.class,
            getTarget(defaultContext.getBean(IUserEventSender.class)));
    }

    @Test
    public void
    testOverriddenEventSenderReplacesDefault()
    {
        assertInstanceOf(
            MockUserEventSender.class,
            getTarget(extendedContext.getBean(IUserEventSender.class)));
    }

    @Test
    public void
    testDefaultMapperDoesNotSkipNulls()
    {
        ModelMapper mapper = defaultContext.getBean(ModelMapper.class);

        assertFalse(mapper.getConfiguration().isSkipNullEnabled());
    }

    @Test
    public void
    testConfigureMapperExtensionIsApplied()
    {
        ModelMapper mapper = extendedContext.getBean(ModelMapper.class);

        assertTrue(mapper.getConfiguration().isSkipNullEnabled());
    }

    @Test
    public void
    testConfigureMapperExtensionRetainsBaseMappings()
    {
        ModelMapper mapper = extendedContext.getBean(ModelMapper.class);
        User        entity =
            new User()
                .setPrimaryId(1L)
                .setUserName("alice@example.com")
                .setValue("preferredTheme","dark");

        UserData data = mapper.map(entity,UserData.class);

        assertEquals(1L,data.getUserId());
        assertEquals("dark",data.getValues().get("preferredTheme"));
    }

    @Test
    public void
    testCommandOpenApiTargetsCommandService()
    {
        GroupedOpenApi openApi = defaultContext.getBean(GroupedOpenApi.class);

        assertTrue(openApi.getPathsToMatch().contains("/user-command-service/**"));
        assertTrue(openApi.getPathsToMatch().contains("/health/**"));
    }

    @Test
    public void
    testSwaggerPathsArePublicOutsideProduction()
    {
        for (String profile : new String[] {"local","development","test"})
        {
            List<String> paths = new GraniteUserQueryServerConfiguration().getPublicPathPatterns(environment(profile));

            assertTrue(paths.containsAll(SWAGGER_PATHS),profile);
        }
    }

    @Test
    public void
    testSwaggerPathsAreNotPublicInProduction()
    {
        List<String> paths = new GraniteUserQueryServerConfiguration().getPublicPathPatterns(environment("production"));

        for (String path : SWAGGER_PATHS)
            assertFalse(paths.contains(path),path);
    }

    private Object
    getTarget(Object bean)
    {
        return
            bean instanceof ScopedObject scoped
                ? scoped.getTargetObject()
                : bean;
    }

    private static MockEnvironment
    environment(String profile)
    {
        MockEnvironment environment = new MockEnvironment();

        environment.setActiveProfiles(profile);
        return environment;
    }
}

//////////////////////////////////////////////////////////////////////////////
