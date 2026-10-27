//////////////////////////////////////////////////////////////////////////////
// AbstractGraniteFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.frontendcontroller.server.application.GraniteFrontEndControllerContext;
import granite.frontendcontroller.server.application.PreferenceDefinition;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.health.FrontEndControllerHealthCheckConfiguration;
import granite.frontendcontroller.server.platform.ForwardedBearerClientRequestFilter;
import granite.frontendcontroller.server.platform.ResponseCookieAdvice;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import strata.foundation.core.concurrent.BoundedExecutor;
import strata.foundation.core.configuration.IConfiguration;
import strata.frontendcontroller.server.application.ApplicationConfiguration;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;

@Configuration
@Import({ApplicationConfiguration.class,FrontEndControllerHealthCheckConfiguration.class})
public abstract
class AbstractGraniteFrontEndControllerConfiguration<S extends IGraniteFrontEndControllerService>
{
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

    protected GraniteClientSettings
    createClientConfiguration(String baseUrlProperty)
    {
        ClientBuilder builder =
            ClientBuilder
                .newBuilder()
                .register(ForwardedBearerClientRequestFilter.fromCurrentRequest())
                .sslContext(
                    sslBundles
                        .getBundle("granite-services")
                        .createSslContext());

        return new GraniteClientSettings(builder,configuration.getProperty(baseUrlProperty));
    }
}

//////////////////////////////////////////////////////////////////////////////
