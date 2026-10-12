//////////////////////////////////////////////////////////////////////////////
// HostConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.queryhost.main;

import granite.session.server.application.ApplicationConfiguration;
import granite.session.server.domain.DomainConfiguration;
import granite.session.server.platform.QueryPlatformConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.configuration.SecureConfiguration;
import strata.server.spring.service.ServiceConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableTransactionManagement
@Import({
    ApplicationConfiguration.class,
    DomainConfiguration.class,
    QueryPlatformConfiguration.class,
    ServiceConfiguration.class})
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
    @Scope("singleton")
    public IConfiguration
    configuration(
        Environment environment,
        @Value("${granite.deploy-env:production}") String deployEnv)
        throws IOException
    {
        String resource = deployEnv + ".properties";

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource))
        {
            if (in == null)
                throw new IllegalStateException("missing classpath resource " + resource);

            String text =
                environment.resolveRequiredPlaceholders(
                    new String(in.readAllBytes(),StandardCharsets.UTF_8));

            return
                new SecureConfiguration(
                    new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Bean
    @Scope("singleton")
    public InitializingBean
    schemaInitializer(
        @Qualifier("&localContainerEntityManagerFactoryBean")
        LocalContainerEntityManagerFactoryBean factory)
    {
        return
            () ->
                new ResourceDatabasePopulator(new ClassPathResource("schema.sql"))
                    .execute(factory.getDataSource());
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
}

//////////////////////////////////////////////////////////////////////////////
