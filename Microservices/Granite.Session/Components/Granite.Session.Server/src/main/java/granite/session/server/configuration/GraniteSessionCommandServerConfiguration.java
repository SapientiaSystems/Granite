//////////////////////////////////////////////////////////////////////////////
// GraniteSessionCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteAuthorities;
import granite.session.server.application.CatalogRoleLoginGate;
import granite.session.server.application.IJwkSetProvider;
import granite.session.server.application.IJwtTokenService;
import granite.session.server.application.ILoginGate;
import granite.session.server.application.IOktaClient;
import granite.session.server.application.IRefreshTokenService;
import granite.session.server.application.IServiceTokenProvider;
import granite.session.server.application.IServiceTokenIssuer;
import granite.session.server.application.JwkSetProvider;
import granite.session.server.application.JwtTokenService;
import granite.session.server.application.OktaClient;
import granite.session.server.application.OktaGroupMappingCheck;
import granite.session.server.application.OktaRoleAssignment;
import granite.session.server.application.OktaSettings;
import granite.session.server.application.OktaUnmappedGroups;
import granite.session.server.application.RefreshTokenService;
import granite.session.server.application.RequiredAuthoritiesCheck;
import granite.session.server.application.RequiredAuthoritiesLoginGate;
import granite.session.server.application.ServiceClientRegistration;
import granite.session.server.application.ServiceTokenIssuer;
import granite.session.server.application.ServiceTokenProvider;
import granite.session.server.platform.CommandPlatformConfiguration;
import granite.session.server.platform.JwtKeyLoader;
import granite.session.server.platform.OktaKeyLoader;
import granite.session.server.platform.OutboxEventSessionEventSender;
import granite.session.server.platform.ServiceTokenClientRequestFilter;
import granite.session.server.platform.SessionPurgeJob;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;
import granite.session.service.event.ISessionEventSender;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.client.requestreply.UserCommandServiceClient;
import granite.user.client.requestreply.UserQueryServiceClient;
import jakarta.ws.rs.client.ClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.annotation.RequestScope;
import strata.outbox.core.repository.IOutboxEventRepository;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@Import({CommandPlatformConfiguration.class})
public
class GraniteSessionCommandServerConfiguration
    extends AbstractGraniteSessionServerConfiguration
{
    @Bean
    @RequestScope
    public ISessionEventSender
    sessionEventSender(IOutboxEventRepository repository)
    {
        return new OutboxEventSessionEventSender(repository);
    }

    @Bean
    @Scope("singleton")
    public IJwtTokenService
    jwtTokenService(Environment environment,ResourceLoader loader)
    {
        KeyPair keys =
            new JwtKeyLoader(environment,loader)
                .load(
                    environment.getProperty(
                        "granite.session.jwt.private-key-location",
                        "classpath:jwt-test-private-key.pem"),
                    readPublicKeyLocation(environment));

        return
            new JwtTokenService(
                (RSAPrivateKey)keys.getPrivate(),
                (RSAPublicKey)keys.getPublic(),
                environment.getProperty("granite.session.jwt.issuer","granite-session"),
                readAudience(environment),
                Duration.parse(environment.getProperty("granite.session.jwt.ttl","PT15M")));
    }

    @Bean
    @Scope("singleton")
    public IJwkSetProvider
    jwkSetProvider(Environment environment,ResourceLoader loader)
    {
        JwtKeyLoader       keyLoader  = new JwtKeyLoader(environment,loader);
        List<RSAPublicKey> publicKeys = new ArrayList<>();
        String             previous   =
            environment.getProperty("granite.session.jwt.previous-public-key-location","");

        publicKeys.add(keyLoader.loadPublic("public-key-location",readPublicKeyLocation(environment)));

        if (!previous.isBlank())
            publicKeys.add(keyLoader.loadPublic("previous-public-key-location",previous));

        return new JwkSetProvider(publicKeys);
    }

    @Bean
    @Scope("singleton")
    public IRefreshTokenService
    refreshTokenService()
    {
        return new RefreshTokenService();
    }

    @Bean
    @Scope("singleton")
    public OktaSettings
    oktaSettings(Environment environment)
    {
        return
            new OktaSettings()
                .setEnabled(environment.getProperty("granite.session.login.okta.enabled",Boolean.class,false))
                .setIssuer(environment.getProperty("granite.session.okta.issuer"))
                .setClientId(environment.getProperty("granite.session.okta.client-id"))
                .setRedirectUri(environment.getProperty("granite.session.okta.redirect-uri"))
                .setRoleAssignment(
                    OktaRoleAssignment.valueOf(
                        environment.getProperty("granite.session.okta.role-assignment","ON_CREATE").trim().toUpperCase()))
                .setUnmappedGroups(
                    OktaUnmappedGroups.valueOf(
                        environment.getProperty("granite.session.okta.unmapped-groups","DEFAULT_ROLE").trim().toUpperCase()))
                .setGroupMapping(
                    Binder
                        .get(environment)
                        .bind("granite.session.okta.group-mapping",Bindable.mapOf(String.class,String.class))
                        .orElse(Map.of()))
                .setTransactionTtl(Duration.parse(environment.getProperty("granite.session.okta.transaction-ttl","PT5M")))
                .setDiscoveryCacheTtl(Duration.parse(environment.getProperty("granite.session.okta.discovery-cache-ttl","PT1H")))
                .setClockSkew(Duration.parse(environment.getProperty("granite.session.okta.clock-skew","PT60S")))
                .setHttpTimeout(Duration.parse(environment.getProperty("granite.session.okta.http-timeout","PT5S")));
    }

    @Bean
    @Scope("singleton")
    public IOktaClient
    oktaClient(OktaSettings settings,Environment environment,ResourceLoader loader,Clock clock)
    {
        return
            new OktaClient(
                settings,
                settings.isEnabled() ? new OktaKeyLoader(environment,loader).load(clock) : null,
                clock);
    }

    @Bean
    @Scope("singleton")
    public SessionPurgeJob
    sessionPurgeJob(
        ObjectProvider<ISessionRepository>              repositories,
        ObjectProvider<IOktaLoginTransactionRepository> transactionRepositories,
        ObjectProvider<ISpringUnitOfWorkManager>        unitOfWorkManagers,
        Clock                                           clock,
        Environment                                     environment)
    {
        return
            new SessionPurgeJob(
                repositories,
                transactionRepositories,
                unitOfWorkManagers,
                clock,
                Duration.parse(environment.getProperty("granite.session.purge.interval","PT15M")),
                environment.getProperty("granite.session.purge.batch-size",Integer.class,500),
                environment.getProperty("granite.session.purge.enabled",Boolean.class,true));
    }

    @Bean
    @Scope("singleton")
    public IServiceTokenProvider
    serviceTokenProvider(IJwtTokenService jwtTokenService,Clock clock,Environment environment)
    {
        return
            new ServiceTokenProvider(
                jwtTokenService,
                clock,
                environment.getProperty("granite.session.service-token.subject","service:granite-session"),
                Binder
                    .get(environment)
                    .bind("granite.session.service-token.authorities",Bindable.listOf(String.class))
                    .orElse(List.of(GraniteAuthorities.SCOPE_INTERNAL)),
                Duration.parse(environment.getProperty("granite.session.service-token.ttl","PT5M")));
    }

    @Bean
    @Scope("singleton")
    public IServiceTokenIssuer
    serviceTokenIssuer(IJwtTokenService jwtTokenService,Environment environment)
    {
        Map<String,Map<String,String>>  clients       =
            Binder
                .get(environment)
                .bind(
                    "granite.session.service-clients",
                    Bindable.<Map<String,Map<String,String>>>of(
                        ResolvableType.forClassWithGenerics(
                            Map.class,
                            ResolvableType.forClass(String.class),
                            ResolvableType.forClassWithGenerics(Map.class,String.class,String.class))))
                .orElse(Map.of());
        List<ServiceClientRegistration> registrations = new ArrayList<>();

        for (Map.Entry<String,Map<String,String>> client : clients.entrySet())
        {
            String hash = client.getValue().getOrDefault("secret-sha256","");

            if (hash.isBlank())
            {
                LogManager.getLogger(getClass()).warn("service client {} has no secret hash; ignored",client.getKey());
                continue;
            }

            registrations.add(
                new ServiceClientRegistration(
                    client.getKey(),
                    hash,
                    Duration.parse(client.getValue().getOrDefault("lifetime","PT10M"))));
        }

        return new ServiceTokenIssuer(jwtTokenService,registrations);
    }

    @Bean
    @Scope("singleton")
    public ILoginGate
    loginGate(Environment environment,ApplicationRolesManager roles)
    {
        List<String> required =
            Binder
                .get(environment)
                .bind("granite.session.login.required-authorities",Bindable.listOf(String.class))
                .orElse(List.of());

        for (String authority : required)
            if (authority != null && !authority.isBlank())
                return new RequiredAuthoritiesLoginGate(required);

        return new CatalogRoleLoginGate(roles);
    }

    @Bean
    @Scope("singleton")
    public RequiredAuthoritiesCheck
    requiredAuthoritiesCheck(Environment environment,ApplicationRolesManager roles)
    {
        List<String> required =
            Binder
                .get(environment)
                .bind("granite.session.login.required-authorities",Bindable.listOf(String.class))
                .orElse(List.of());

        return new RequiredAuthoritiesCheck(required,roles);
    }

    @Bean
    @Scope("singleton")
    public OktaGroupMappingCheck
    oktaGroupMappingCheck(OktaSettings settings,ApplicationRolesManager roles)
    {
        return new OktaGroupMappingCheck(settings,roles);
    }

    @Bean
    @Scope("singleton")
    public IUserCommandServiceClient
    userCommandServiceClient(
        ObjectProvider<SslBundles> sslBundles,
        Environment                environment,
        IServiceTokenProvider      serviceTokenProvider)
    {
        return
            new UserCommandServiceClient(
                createClientBuilder(sslBundles,environment,serviceTokenProvider),
                environment.getRequiredProperty("granite.user-command-service.host"));
    }

    @Bean
    @Scope("singleton")
    public IUserQueryServiceClient
    userQueryServiceClient(
        ObjectProvider<SslBundles> sslBundles,
        Environment                environment,
        IServiceTokenProvider      serviceTokenProvider)
    {
        return
            new UserQueryServiceClient(
                createClientBuilder(sslBundles,environment,serviceTokenProvider),
                environment.getRequiredProperty("granite.user-query-service.host"));
    }

    @Override
    protected List<String>
    getTokenlessPathPatterns(Environment environment)
    {
        List<String> paths = super.getTokenlessPathPatterns(environment);

        paths.addAll(
            List.of(
                "/session-command-service/login",
                "/session-command-service/refresh",
                "/session-command-service/resolve-session",
                "/session-command-service/issue-service-token",
                "/.well-known/jwks.json"));
        return paths;
    }

    @Override
    protected String
    getServicePathPattern() { return "/session-command-service/**"; }

    protected ClientBuilder
    createClientBuilder(
        ObjectProvider<SslBundles> sslBundles,
        Environment                environment,
        IServiceTokenProvider      serviceTokenProvider)
    {
        ClientBuilder builder =
            ClientBuilder
                .newBuilder()
                .register(new ServiceTokenClientRequestFilter(serviceTokenProvider));
        String        bundle  =
            environment.getProperty("granite.session.user-client.ssl-bundle","");

        if (!bundle.isBlank())
            builder.sslContext(
                sslBundles.getObject().getBundle(bundle).createSslContext());

        return builder;
    }

    private String
    readPublicKeyLocation(Environment environment)
    {
        return
            environment.getProperty(
                "granite.session.jwt.public-key-location",
                "classpath:jwt-test-public-key.pem");
    }

    private List<String>
    readAudience(Environment environment)
    {
        return
            Binder
                .get(environment)
                .bind("granite.session.jwt.audience",Bindable.listOf(String.class))
                .orElse(List.of("granite-services"));
    }
}

//////////////////////////////////////////////////////////////////////////////
