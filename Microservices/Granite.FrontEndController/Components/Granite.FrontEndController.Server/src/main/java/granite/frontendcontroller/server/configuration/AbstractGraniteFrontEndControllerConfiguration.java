//////////////////////////////////////////////////////////////////////////////
// AbstractGraniteFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.foundation.core.security.GraniteJwtCheckSecurity;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.foundation.core.security.JwtCheckMode;
import granite.frontendcontroller.server.application.GraniteFrontEndControllerContext;
import granite.frontendcontroller.server.application.IServiceTokenSource;
import granite.frontendcontroller.server.application.PreferenceDefinition;
import granite.frontendcontroller.server.application.ServiceTokenSource;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.health.FrontEndControllerHealthCheckConfiguration;
import granite.frontendcontroller.server.platform.ResponseCookieAdvice;
import granite.frontendcontroller.server.platform.ServiceTokenClientRequestFilter;
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
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import strata.foundation.core.concurrent.BoundedExecutor;
import strata.foundation.core.configuration.IConfiguration;
import strata.frontendcontroller.server.application.ApplicationConfiguration;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;

@Configuration
@EnableWebSecurity
@Import({ApplicationConfiguration.class,FrontEndControllerHealthCheckConfiguration.class})
public abstract
class AbstractGraniteFrontEndControllerConfiguration<S extends IGraniteFrontEndControllerService>
{
    public static final String SERVICE_CLIENT_ID     = "granite.frontendcontroller.service-client.id";
    public static final String SERVICE_CLIENT_SECRET = "granite.frontendcontroller.service-client.secret";

    @Autowired
    private IConfiguration configuration;

    @Autowired
    private SslBundles     sslBundles;

    @Bean
    @Scope("singleton")
    @Named("FrontEndController")
    public Executor
    frontEndControllerExecutor()
    {
        return new BoundedExecutor(200,Duration.ofSeconds(10));
    }

    @Bean
    @Scope("singleton")
    public ApplicationRolesManager
    applicationRoles()
    {
        return new GraniteApplicationRolesManagerSupplier().get();
    }

    @Bean
    @Scope("singleton")
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
    @Scope("singleton")
    public IServiceTokenSource
    serviceTokenSource()
    {
        return
            new ServiceTokenSource(
                createServiceTokenClient(),
                Clock.systemUTC(),
                configuration.getProperty(SERVICE_CLIENT_ID,"granite-frontendcontroller"),
                configuration.getProperty(SERVICE_CLIENT_SECRET,""));
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
    @Scope("singleton")
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
                roles,
                preferenceDefinitions());
    }

    protected List<PreferenceDefinition>
    preferenceDefinitions()
    {
        return List.of(PreferenceDefinition.enumeration("granite.theme",List.of("light","dark"),"light"));
    }

    protected abstract S
    createService(GraniteFrontEndControllerContext context);

    protected IUserQueryServiceClient
    createUserQueryClient()
    {
        GraniteClientSettings client = createClientConfiguration("user-query-service.base-url");

        return new UserQueryServiceClient(client.getBuilder(),client.getBaseUrl());
    }

    protected IUserCommandServiceClient
    createUserCommandClient()
    {
        GraniteClientSettings client = createClientConfiguration("user-command-service.base-url");

        return new UserCommandServiceClient(client.getBuilder(),client.getBaseUrl());
    }

    protected ISessionCommandServiceClient
    createSessionCommandClient()
    {
        GraniteClientSettings client = createClientConfiguration("session-command-service.base-url");

        return new SessionCommandServiceClient(client.getBuilder(),client.getBaseUrl());
    }

    protected ISessionQueryServiceClient
    createSessionQueryClient()
    {
        GraniteClientSettings client = createClientConfiguration("session-query-service.base-url");

        return new SessionQueryServiceClient(client.getBuilder(),client.getBaseUrl());
    }

    protected ISessionCommandServiceClient
    createServiceTokenClient()
    {
        return
            new SessionCommandServiceClient(
                createClientBuilder(),
                configuration.getProperty("session-command-service.base-url"));
    }

    protected GraniteClientSettings
    createClientConfiguration(String baseUrlProperty)
    {
        ClientBuilder builder =
            createClientBuilder()
                .register(new ServiceTokenClientRequestFilter(serviceTokenSource()));

        return new GraniteClientSettings(builder,configuration.getProperty(baseUrlProperty));
    }

    private ClientBuilder
    createClientBuilder()
    {
        return
            ClientBuilder
                .newBuilder()
                .sslContext(
                    sslBundles
                        .getBundle("granite-services")
                        .createSslContext());
    }
}

//////////////////////////////////////////////////////////////////////////////
