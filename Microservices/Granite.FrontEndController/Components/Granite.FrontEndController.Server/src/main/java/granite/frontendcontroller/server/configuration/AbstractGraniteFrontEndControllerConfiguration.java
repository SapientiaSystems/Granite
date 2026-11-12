//////////////////////////////////////////////////////////////////////////////
// AbstractGraniteFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.feedback.client.requestreply.FeedbackCommandServiceClient;
import granite.agent.client.requestreply.AgentCommandServiceClient;
import granite.agent.client.requestreply.AgentQueryServiceClient;
import granite.agent.client.requestreply.IAgentCommandServiceClient;
import granite.agent.client.requestreply.IAgentQueryServiceClient;
import granite.content.client.requestreply.ContentQueryServiceClient;
import granite.content.client.requestreply.IContentQueryServiceClient;
import granite.survey.client.requestreply.SurveyCommandServiceClient;
import granite.feedback.client.requestreply.FeedbackQueryServiceClient;
import granite.survey.client.requestreply.SurveyQueryServiceClient;
import granite.feedback.client.requestreply.IFeedbackCommandServiceClient;
import granite.survey.client.requestreply.ISurveyCommandServiceClient;
import granite.feedback.client.requestreply.IFeedbackQueryServiceClient;
import granite.survey.client.requestreply.ISurveyQueryServiceClient;
import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteCookieNames;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.foundation.core.security.GraniteJwtCheckSecurity;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.foundation.core.security.JwtCheckMode;
import granite.frontendcontroller.server.application.GraniteFrontEndControllerContext;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.application.IServiceTokenSource;
import granite.frontendcontroller.server.application.PreferenceDefinition;
import granite.frontendcontroller.server.application.ServiceTokenSource;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.health.FrontEndControllerHealthCheckConfiguration;
import granite.frontendcontroller.server.platform.ResponseCookieAdvice;
import granite.frontendcontroller.server.platform.ServiceTokenClientRequestFilter;
import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.client.requestreply.SessionCommandServiceClient;
import granite.session.client.requestreply.SessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.client.requestreply.UserCommandServiceClient;
import granite.user.client.requestreply.UserQueryServiceClient;
import jakarta.inject.Named;
import jakarta.ws.rs.client.ClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import strata.client.core.service.CountedClient;
import strata.client.core.service.ObjectMapperProvider;
import strata.foundation.core.concurrent.BoundedExecutor;
import strata.foundation.core.concurrent.BoundedExecutorService;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.spring.inject.SingletonScoped;
import strata.frontendcontroller.server.application.ApplicationConfiguration;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Configuration
@EnableWebSecurity
@Import({ApplicationConfiguration.class,FrontEndControllerHealthCheckConfiguration.class})
public abstract
class AbstractGraniteFrontEndControllerConfiguration<S extends IGraniteFrontEndControllerService>
{
    public static final String SERVICE_CLIENT_ID     = "granite.frontendcontroller.service-client.id";
    public static final String SERVICE_CLIENT_SECRET = "granite.frontendcontroller.service-client.secret";
    public static final String SSL_BUNDLE            = "granite.frontendcontroller.ssl-bundle";

    @Autowired
    private IConfiguration configuration;

    @Autowired
    private SslBundles     sslBundles;

    @Bean
    @SingletonScoped
    @Named("FrontEndController")
    public Executor
    frontEndControllerExecutor()
    {
        return new BoundedExecutor(200,Duration.ofSeconds(10));
    }

    @Bean
    @SingletonScoped
    public ApplicationRolesManager
    applicationRoles()
    {
        return new GraniteApplicationRolesManagerSupplier().get();
    }

    @Bean
    @SingletonScoped
    public InitializingBean
    applicationRolesStartupLog(ApplicationRolesManager roles)
    {
        return
            () ->
                LogManager
                    .getLogger(getClass())
                    .info(
                        "application role catalog: {} (default {})",
                        roles.getRoleNames(),
                        roles.getDefaultRole());
    }

    @Bean
    public ResponseCookieAdvice
    responseCookieAdvice()
    {
        return new ResponseCookieAdvice();
    }

    @Bean
    @Lazy
    @SingletonScoped
    public IServiceTokenSource
    serviceTokenSource()
    {
        return
            new ServiceTokenSource(
                createServiceTokenClient(),
                Clock.systemUTC(),
                configuration.getProperty(SERVICE_CLIENT_ID,"granite-frontendcontroller"),
                configuration.getProperty(SERVICE_CLIENT_SECRET,""),
                ServiceTokenSource.DEFAULT_ISSUE_TIMEOUT);
    }

    @Bean
    public SecurityFilterChain
    securityFilterChain(
        HttpSecurity http,
        JwtDecoder   jwtDecoder,
        Environment  environment)
        throws Exception
    {
        return
            new GraniteJwtCheckSecurity()
                .jwtCheckChain(
                    http,
                    jwtDecoder,
                    JwtCheckMode.OPTIONAL,
                    getTokenlessPathPatterns(environment));
    }

    @Bean
    @SingletonScoped
    public JwtDecoder
    jwtDecoder(Environment environment)
    {
        return
            new GraniteJwtDecoderFactory()
                .create(
                    GraniteResourceServerSettings.from(environment),
                    createJwkSetRestOperations(environment));
    }

    protected RestOperations
    createJwkSetRestOperations(Environment environment)
    {
        String bundle = environment.getProperty(GraniteResourceServerSettings.SSL_BUNDLE,"");

        if (bundle.isBlank())
            return new RestTemplate();

        return
            new RestTemplateBuilder()
                .sslBundle(sslBundles.getBundle(bundle))
                .build();
    }

    protected List<String>
    getTokenlessPathPatterns(Environment environment)
    {
        return new GraniteJwtCheckSecurity().defaultTokenlessPaths(environment);
    }

    protected GraniteFrontEndControllerContext
    createContext(ModelMapper mapper,Executor executor,ApplicationRolesManager roles)
    {
        return
            new GraniteFrontEndControllerContext(
                mapper,
                executor,
                createUserQueryClient(),
                createUserCommandClient(),
                createSessionCommandClient(),
                createSessionQueryClient(),
                SessionRequestContext.fromCurrentRequest(),
                SessionLoginSettings.fromConfiguration(configuration),
                sessionCookies(),
                roles,
                preferenceDefinitions(),
                createHelpServiceClients());
    }

    protected GraniteHelpServiceClients
    createHelpServiceClients()
    {
        return
            new GraniteHelpServiceClients()
                .setFeedbackCommandClient(this::createFeedbackCommandClient)
                .setFeedbackQueryClient(this::createFeedbackQueryClient)
                .setSurveyCommandClient(this::createSurveyCommandClient)
                .setSurveyQueryClient(this::createSurveyQueryClient)
                .setContentQueryClient(this::createContentQueryClient)
                .setAgentCommandClient(this::createAgentCommandClient)
                .setAgentQueryClient(this::createAgentQueryClient);
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public IFeedbackCommandServiceClient
    feedbackCommandServiceClient()
    {
        return new FeedbackCommandServiceClient(graniteServiceClient(),getBaseUrl("feedback-command-service.base-url"));
    }

    protected IFeedbackCommandServiceClient
    createFeedbackCommandClient()
    {
        return feedbackCommandServiceClient();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public IFeedbackQueryServiceClient
    feedbackQueryServiceClient()
    {
        return new FeedbackQueryServiceClient(graniteServiceClient(),getBaseUrl("feedback-query-service.base-url"));
    }

    protected IFeedbackQueryServiceClient
    createFeedbackQueryClient()
    {
        return feedbackQueryServiceClient();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public ISurveyCommandServiceClient
    surveyCommandServiceClient()
    {
        return new SurveyCommandServiceClient(graniteServiceClient(),getBaseUrl("survey-command-service.base-url"));
    }

    protected ISurveyCommandServiceClient
    createSurveyCommandClient()
    {
        return surveyCommandServiceClient();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public ISurveyQueryServiceClient
    surveyQueryServiceClient()
    {
        return new SurveyQueryServiceClient(graniteServiceClient(),getBaseUrl("survey-query-service.base-url"));
    }

    protected ISurveyQueryServiceClient
    createSurveyQueryClient()
    {
        return surveyQueryServiceClient();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public IContentQueryServiceClient
    contentQueryServiceClient()
    {
        return new ContentQueryServiceClient(graniteServiceClient(),getBaseUrl("content-query-service.base-url"));
    }

    protected IContentQueryServiceClient
    createContentQueryClient()
    {
        return contentQueryServiceClient();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public IAgentCommandServiceClient
    agentCommandServiceClient()
    {
        return new AgentCommandServiceClient(graniteServiceClient(),getBaseUrl("agent-command-service.base-url"));
    }

    protected IAgentCommandServiceClient
    createAgentCommandClient()
    {
        return agentCommandServiceClient();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public IAgentQueryServiceClient
    agentQueryServiceClient()
    {
        return new AgentQueryServiceClient(graniteServiceClient(),getBaseUrl("agent-query-service.base-url"));
    }

    protected IAgentQueryServiceClient
    createAgentQueryClient()
    {
        return agentQueryServiceClient();
    }

    @Bean
    @SingletonScoped
    public SessionCookies
    sessionCookies()
    {
        return new SessionCookies(cookieNames());
    }

    protected GraniteCookieNames
    cookieNames()
    {
        return new GraniteCookieNames(configuration.getProperty(GraniteCookieNames.PREFIX,GraniteCookieNames.DEFAULT_PREFIX));
    }

    protected List<PreferenceDefinition>
    preferenceDefinitions()
    {
        return List.of(PreferenceDefinition.enumeration("granite.theme",List.of("light","dark"),"light"));
    }

    protected abstract S
    createService(GraniteFrontEndControllerContext context);

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public IUserQueryServiceClient
    userQueryServiceClient()
    {
        return new UserQueryServiceClient(graniteServiceClient(),getBaseUrl("user-query-service.base-url"));
    }

    protected IUserQueryServiceClient
    createUserQueryClient()
    {
        return userQueryServiceClient();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public IUserCommandServiceClient
    userCommandServiceClient()
    {
        return new UserCommandServiceClient(graniteServiceClient(),getBaseUrl("user-command-service.base-url"));
    }

    protected IUserCommandServiceClient
    createUserCommandClient()
    {
        return userCommandServiceClient();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public ISessionCommandServiceClient
    sessionCommandServiceClient()
    {
        return new SessionCommandServiceClient(graniteServiceClient(),getBaseUrl("session-command-service.base-url"));
    }

    protected ISessionCommandServiceClient
    createSessionCommandClient()
    {
        return sessionCommandServiceClient();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public ISessionQueryServiceClient
    sessionQueryServiceClient()
    {
        return new SessionQueryServiceClient(graniteServiceClient(),getBaseUrl("session-query-service.base-url"));
    }

    protected ISessionQueryServiceClient
    createSessionQueryClient()
    {
        return sessionQueryServiceClient();
    }

    protected ISessionCommandServiceClient
    createServiceTokenClient()
    {
        return
            new SessionCommandServiceClient(
                createClientBuilder(),
                configuration.getProperty("session-command-service.base-url"));
    }

    @Bean(destroyMethod = "shutdown")
    @Lazy
    @SingletonScoped
    public ExecutorService
    graniteServiceClientExecutor()
    {
        return BoundedExecutorService.of(100,Duration.ofSeconds(30));
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @SingletonScoped
    public CountedClient
    graniteServiceClient()
    {
        return
            CountedClient.ofCountedClient(
                createServiceClientBuilder()
                    .executorService(graniteServiceClientExecutor())
                    .register(new ObjectMapperProvider())
                    .build());
    }

    protected GraniteClientSettings
    createClientConfiguration(String baseUrlProperty)
    {
        return new GraniteClientSettings(createServiceClientBuilder(),configuration.getProperty(baseUrlProperty));
    }

    private ClientBuilder
    createServiceClientBuilder()
    {
        return
            createClientBuilder()
                .register(new ServiceTokenClientRequestFilter(serviceTokenSource()));
    }

    private String
    getBaseUrl(String baseUrlProperty)
    {
        return Objects.requireNonNull(configuration.getProperty(baseUrlProperty),baseUrlProperty);
    }

    private ClientBuilder
    createClientBuilder()
    {
        return
            ClientBuilder
                .newBuilder()
                .sslContext(
                    sslBundles
                        .getBundle(configuration.getProperty(SSL_BUNDLE,"granite-services"))
                        .createSslContext());
    }
}

//////////////////////////////////////////////////////////////////////////////
