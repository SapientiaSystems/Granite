//////////////////////////////////////////////////////////////////////////////
// AbstractGraniteSessionServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import granite.foundation.core.security.*;
import granite.foundation.core.security.ApplicationRolesManager;
import granite.session.server.application.ApplicationConfiguration;
import granite.session.server.application.IJwkSetProvider;
import granite.session.server.application.MappingModule;
import granite.session.server.domain.DomainConfiguration;
import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestScope;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.spring.inject.SpringConfigurationProvider;
import strata.foundation.spring.mapper.StrataModelResolver;
import strata.foundation.spring.mapper.StrataObjectMapperProvider;
import strata.server.spring.service.ServiceConfiguration;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableTransactionManagement
@EnableWebSecurity
@Import({
    ApplicationConfiguration.class,
    DomainConfiguration.class,
    ServiceConfiguration.class,
    SpringDocConfigProperties.class,
    StrataModelResolver.class})
public abstract
class AbstractGraniteSessionServerConfiguration
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
    @Scope("singleton")
    public IConfiguration
    configuration(Environment environment)
    {
        return new SpringConfigurationProvider(environment).get();
    }

    @Bean
    @Scope("singleton")
    public ModelMapper
    mapper()
    {
        return configureMapper(new ModelMapper());
    }

    @Bean
    @Scope("singleton")
    public ApplicationRolesManager
    applicationRoles()
    {
        return GraniteApplicationRoles.create();
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
    @Scope("singleton")
    public Clock
    clock()
    {
        return Clock.systemUTC();
    }

    @Bean
    @Scope("singleton")
    public InitializingBean
    schemaInitializer(EntityManagerFactory factory)
    {
        return
            () ->
                new ResourceDatabasePopulator(new ClassPathResource("schema.sql"))
                    .execute(((EntityManagerFactoryInfo)factory).getDataSource());
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
        HttpSecurity                      http,
        JwtDecoder                        jwtDecoder,
        GraniteJwtAuthenticationConverter jwtAuthenticationConverter,
        Environment                       environment)
        throws Exception
    {
        List<String> publicPaths = getPublicPathPatterns(environment);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                requests ->
                {
                    requests.requestMatchers(antMatcher(HttpMethod.OPTIONS,"/**")).permitAll();

                    for (String path : publicPaths)
                        requests.requestMatchers(antMatcher(path)).permitAll();

                    requests.anyRequest().authenticated();
                })
            .oauth2ResourceServer(
                server ->
                    server.jwt(
                        jwt ->
                            jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    @Bean
    @Scope("singleton")
    public JwtDecoder
    jwtDecoder(
        Environment                     environment,
        ObjectProvider<IJwkSetProvider> localKeys,
        ObjectProvider<SslBundles>      sslBundles)
    {
        GraniteResourceServerSettings settings = GraniteResourceServerSettings.from(environment);
        GraniteJwtDecoderFactory      factory  = new GraniteJwtDecoderFactory();
        IJwkSetProvider               provider = localKeys.getIfAvailable();

        if (provider != null && settings.getJwkSetUri().isEmpty())
            return factory.create(provider.getPublicJwkSet(),settings);

        return factory.create(settings,createJwkSetRestOperations(sslBundles,environment));
    }

    @Bean
    @Scope("singleton")
    public GraniteJwtAuthenticationConverter
    jwtAuthenticationConverter()
    {
        return new GraniteJwtAuthenticationConverter();
    }

    @Bean
    @Scope("singleton")
    public GroupedOpenApi
    openApi()
    {
        return
            GroupedOpenApi
                .builder()
                .group("Session")
                .pathsToMatch(
                    getServicePathPattern(),
                    "/health/**")
                .packagesToScan(
                    "granite.session.server.platform",
                    "strata.server.spring.health")
                .build();
    }

    @Bean
    @Scope("singleton")
    public ObjectMapperProvider
    objectMapperProvider(SpringDocConfigProperties properties)
    {
        return new StrataObjectMapperProvider(properties);
    }

    @Bean
    @Scope("singleton")
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
    getPublicPathPatterns(Environment environment)
    {
        List<String> paths =
            new ArrayList<>(
                List.of(
                    environment.getProperty("strata.health.base-path","/health") + "/**",
                    "/error"));

        if (!environment.acceptsProfiles(Profiles.of("production")))
            paths.addAll(
                List.of(
                    environment.getProperty("springdoc.api-docs.path","/v3/api-docs") + "/**",
                    environment.getProperty("springdoc.swagger-ui.path","/swagger-ui") + "/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"));

        return paths;
    }

    protected abstract String
    getServicePathPattern();
}

//////////////////////////////////////////////////////////////////////////////
