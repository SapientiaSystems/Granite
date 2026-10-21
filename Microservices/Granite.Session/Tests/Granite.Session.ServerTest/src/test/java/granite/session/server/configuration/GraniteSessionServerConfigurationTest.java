//////////////////////////////////////////////////////////////////////////////
// GraniteSessionServerConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import granite.session.server.domain.Session;
import granite.session.server.platform.OutboxEventSessionEventSender;
import granite.session.server.platform.PlatformContextTestConfiguration;
import granite.session.service.event.ISessionEventSender;
import granite.session.service.requestreply.SessionData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.time.Clock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteSessionServerConfigurationTest
{
    private static final List<String> SWAGGER_PATHS = List.of("/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**");

    @Test
    public void
    testSwaggerPathsArePublicOutsideProduction()
    {
        for (String profile : new String[] {"local","development","test"})
        {
            List<String> paths = new GraniteSessionQueryServerConfiguration().getPublicPathPatterns(environment(profile));

            assertTrue(paths.containsAll(SWAGGER_PATHS),profile);
        }
    }

    @Test
    public void
    testSwaggerPathsAreNotPublicInProduction()
    {
        List<String> paths = new GraniteSessionQueryServerConfiguration().getPublicPathPatterns(environment("production"));

        for (String path : SWAGGER_PATHS)
            assertFalse(paths.contains(path),path);
    }

    private static AnnotationConfigApplicationContext defaultContext;
    private static AnnotationConfigApplicationContext extendedContext;

    @BeforeAll
    public static void
    initialize()
    {
        defaultContext  = newContext(PlatformContextTestConfiguration.class);
        extendedContext = newContext(ExtendedSessionCommandServerConfiguration.class);
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
            OutboxEventSessionEventSender.class,
            getTarget(defaultContext.getBean(ISessionEventSender.class)));
    }

    @Test
    public void
    testOverriddenEventSenderReplacesDefault()
    {
        assertTrue(
            Mockito
                .mockingDetails(getTarget(extendedContext.getBean(ISessionEventSender.class)))
                .isMock());
    }

    @Test
    public void
    testOverriddenClockReplacesDefault()
    {
        assertEquals(
            ExtendedSessionCommandServerConfiguration.FIXED_INSTANT,
            extendedContext.getBean(Clock.class).instant());
    }

    @Test
    public void
    testDefaultMapperDoesNotSkipNulls()
    {
        assertFalse(
            defaultContext
                .getBean(ModelMapper.class)
                .getConfiguration()
                .isSkipNullEnabled());
    }

    @Test
    public void
    testConfigureMapperExtensionIsApplied()
    {
        assertTrue(
            extendedContext
                .getBean(ModelMapper.class)
                .getConfiguration()
                .isSkipNullEnabled());
    }

    @Test
    public void
    testConfigureMapperExtensionRetainsBaseMappings()
    {
        ModelMapper mapper = extendedContext.getBean(ModelMapper.class);
        Session     entity =
            new Session()
                .setSessionId(7L)
                .setUserId(42L)
                .setValue("cart-id","c-1");

        SessionData data = mapper.map(entity,SessionData.class);

        assertEquals(7L,data.getSessionId());
        assertEquals(42L,data.getUserId());
        assertEquals("c-1",data.getValues().get("cart-id"));
    }

    @Test
    public void
    testCommandOpenApiTargetsCommandService()
    {
        GroupedOpenApi openApi = defaultContext.getBean(GroupedOpenApi.class);

        assertTrue(openApi.getPathsToMatch().contains("/session-command-service/**"));
        assertTrue(openApi.getPathsToMatch().contains("/health/**"));
    }

    private static AnnotationConfigApplicationContext
    newContext(Class<?> configuration)
    {
        AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext();

        context.getEnvironment().setActiveProfiles("local");
        context.register(configuration);
        context.refresh();
        return context;
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
