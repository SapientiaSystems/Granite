//////////////////////////////////////////////////////////////////////////////
// HostConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.adminhost.main;

import granite.frontendcontroller.server.configuration.GraniteAdminFrontEndControllerConfiguration;
import granite.frontendcontroller.server.health.FrontEndControllerHealthCheckConfiguration;
import granite.frontendcontroller.service.requestreply.FrontEndJacksonModule;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestScope;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.spring.inject.SpringConfigurationProvider;
import strata.foundation.spring.mapper.StrataModelResolver;
import strata.foundation.spring.mapper.StrataObjectMapperProvider;
import strata.server.spring.service.ServiceConfiguration;

@Configuration
@Import({
    GraniteAdminFrontEndControllerConfiguration.class,
    ServiceConfiguration.class,
    SpringDocConfigProperties.class,
    StrataModelResolver.class,
    FrontEndControllerHealthCheckConfiguration.class})
public
class HostConfiguration
{
    @Bean
    public InitializingBean
    initialize()
    {
        return
            () ->
                SecurityContextHolder
                    .setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public BeanFactoryPostProcessor
    beanFactoryPostProcessor()
    {
        return
            factory ->
                factory.registerScope(
                    "request",
                    new RequestScope());
    }

    @Bean
    @Scope("singleton")
    public IConfiguration
    configuration()
    {
        return
            new SpringConfigurationProvider().get();
    }

    @Bean
    public CommonsRequestLoggingFilter
    requestLoggingFilter()
    {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }

    @Bean
    @Scope("singleton")
    public GroupedOpenApi
    openApi()
    {
        return
            GroupedOpenApi
                .builder()
                .group("FrontEndController")
                .pathsToMatch(
                    "/frontendcontroller-admin-service/**",
                    "/health/**")
                .packagesToScan(
                    "granite.frontendcontroller.server.platform",
                    "strata.server.spring.health")
                .build();
    }

    @Bean
    @Scope("singleton")
    public ObjectMapperProvider
    objectMapperProvider(SpringDocConfigProperties properties)
    {
        StrataObjectMapperProvider provider = new StrataObjectMapperProvider(properties);

        provider.jsonMapper().registerModule(new FrontEndJacksonModule());
        provider.yamlMapper().registerModule(new FrontEndJacksonModule());
        return provider;
    }

    @Bean
    @Scope("singleton")
    public SpringDocConfigProperties
    springDocConfigProperties()
    {
        return new SpringDocConfigProperties();
    }
}

//////////////////////////////////////////////////////////////////////////////
