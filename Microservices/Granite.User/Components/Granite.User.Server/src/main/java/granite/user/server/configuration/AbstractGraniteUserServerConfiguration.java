//////////////////////////////////////////////////////////////////////////////
// AbstractGraniteUserServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.configuration;

import granite.foundation.core.security.GraniteJwtCheckSecurity;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.JwtCheckMode;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.user.server.application.ApplicationConfiguration;
import granite.user.server.application.LoginLockoutPolicy;
import granite.user.server.application.MappingModule;
import granite.user.server.application.PasswordPolicy;
import granite.user.server.domain.DomainConfiguration;
import org.apache.logging.log4j.LogManager;
import org.modelmapper.ModelMapper;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestScope;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.spring.inject.SingletonScoped;
import strata.foundation.spring.inject.SpringConfigurationProvider;
import strata.foundation.spring.mapper.StrataModelResolver;
import strata.foundation.spring.mapper.StrataObjectMapperProvider;
import strata.server.spring.service.ServiceConfiguration;

import java.time.Clock;
import java.time.Duration;
import java.util.List;


@EnableTransactionManagement
@EnableWebSecurity
@Import({
    ApplicationConfiguration.class,
    DomainConfiguration.class,
    ServiceConfiguration.class,
    SpringDocConfigProperties.class,
    StrataModelResolver.class})
public abstract
class AbstractGraniteUserServerConfiguration
{
    @Bean
    public BeanFactoryPostProcessor
    beanFactoryPostProcessor()
    {
        return
            factory ->
            {
                SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
                factory.registerScope("request",new RequestScope());
            };
    }

    @Bean
    @SingletonScoped
    public IConfiguration
    configuration(Environment environment)
    {
        return new SpringConfigurationProvider(environment).get();
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
    @SingletonScoped
    public ModelMapper
    mapper()
    {
        return configureMapper(new ModelMapper());
    }

    @Bean
    @SingletonScoped
    public PasswordEncoder
    passwordEncoder()
    {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @SingletonScoped
    public LoginLockoutPolicy
    loginLockoutPolicy(Environment environment)
    {
        return
            new LoginLockoutPolicy(
                environment.getProperty("granite.user.lockout.max-attempts",Integer.class,5),
                Duration.parse(environment.getProperty("granite.user.lockout.duration","PT15M")),
                Clock.systemUTC());
    }

    @Bean
    @SingletonScoped
    public PasswordPolicy
    passwordPolicy(Environment environment)
    {
        return
            new PasswordPolicy(
                environment.getProperty("granite.user.password.min-length",Integer.class,8),
                PasswordPolicy.MAX_LENGTH);
    }

    @Bean
    public CommonsRequestLoggingFilter
    requestLoggingFilter()
    {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
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
                    JwtCheckMode.REQUIRED,
                    getTokenlessPathPatterns(environment));
    }

    @Bean
    @SingletonScoped
    public JwtDecoder
    jwtDecoder(Environment environment,ObjectProvider<SslBundles> sslBundles)
    {
        return
            new GraniteJwtDecoderFactory()
                .create(
                    GraniteResourceServerSettings.from(environment),
                    createJwkSetRestOperations(sslBundles,environment));
    }

    @Bean
    @SingletonScoped
    public GroupedOpenApi
    openApi()
    {
        return
            GroupedOpenApi
                .builder()
                .group("User")
                .pathsToMatch(
                    getServicePathPattern(),
                    "/health/**")
                .packagesToScan(
                    "granite.user.server.platform",
                    "strata.server.spring.health")
                .build();
    }

    @Bean
    @SingletonScoped
    public ObjectMapperProvider
    objectMapperProvider(SpringDocConfigProperties properties)
    {
        return new StrataObjectMapperProvider(properties);
    }

    @Bean
    @SingletonScoped
    public SpringDocConfigProperties
    springDocConfigProperties()
    {
        return new SpringDocConfigProperties();
    }

    protected ModelMapper
    configureMapper(ModelMapper mapper)
    {
        return mapper.registerModule(new MappingModule());
    }

    protected RestOperations
    createJwkSetRestOperations(ObjectProvider<SslBundles> sslBundles,Environment environment)
    {
        String bundle = environment.getProperty(GraniteResourceServerSettings.SSL_BUNDLE,"");

        if (bundle.isBlank())
            return new RestTemplate();

        return
            new RestTemplateBuilder()
                .sslBundle(sslBundles.getObject().getBundle(bundle))
                .build();
    }

    protected List<String>
    getTokenlessPathPatterns(Environment environment)
    {
        return new GraniteJwtCheckSecurity().defaultTokenlessPaths(environment);
    }

    protected abstract String
    getServicePathPattern();
}

//////////////////////////////////////////////////////////////////////////////
