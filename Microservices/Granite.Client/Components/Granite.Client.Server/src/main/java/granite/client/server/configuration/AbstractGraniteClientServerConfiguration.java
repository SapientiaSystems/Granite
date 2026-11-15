//////////////////////////////////////////////////////////////////////////////
// AbstractGraniteClientServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.configuration;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.foundation.core.security.GraniteJwtCheckSecurity;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.foundation.core.security.JwtCheckMode;
import granite.client.server.application.ApplicationConfiguration;
import granite.client.server.application.ClientIdGenerator;
import granite.client.server.application.MappingModule;
import granite.client.server.domain.DomainConfiguration;
import granite.client.server.platform.JwtCallerSubject;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@EnableTransactionManagement
@EnableWebSecurity
@Import({
    ApplicationConfiguration.class,
    DomainConfiguration.class,
    ServiceConfiguration.class,
    SpringDocConfigProperties.class,
    StrataModelResolver.class})
public abstract
class AbstractGraniteClientServerConfiguration
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
    public ModelMapper
    mapper()
    {
        return configureMapper(new ModelMapper());
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
    public ClientIdGenerator
    clientIdGenerator(Environment environment)
    {
        return
            new ClientIdGenerator(
                environment.getProperty(ClientIdGenerator.PREFIX_PROPERTY,ClientIdGenerator.DEFAULT_PREFIX));
    }

    @Bean
    @SingletonScoped
    public Supplier<Optional<String>>
    clientCallerSubject()
    {
        return new JwtCallerSubject();
    }

    @Bean
    @SingletonScoped
    public Clock
    clock()
    {
        return Clock.systemUTC();
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
                .group("Client")
                .pathsToMatch(
                    getServicePathPattern(),
                    "/health/**")
                .packagesToScan(
                    "granite.client.server.platform",
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
