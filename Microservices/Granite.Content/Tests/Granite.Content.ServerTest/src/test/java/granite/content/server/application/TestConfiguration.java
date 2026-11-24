//////////////////////////////////////////////////////////////////////////////
// TestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.content.server.configuration.GraniteContentCommandServerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;
import strata.foundation.spring.inject.SingletonScoped;

@Configuration
@PropertySource({"classpath:application.properties","classpath:application-local.properties"})
public
class TestConfiguration
    extends GraniteContentCommandServerConfiguration
{
    @Override
    @Bean
    @SingletonScoped
    public IConfiguration
    configuration(Environment environment)
    {
        return new ApplicationConfigurationProvider("application-local").get();
    }

    @Bean
    @SingletonScoped
    public ObjectMapper
    objectMapper()
    {
        return new ObjectMapper();
    }
}

//////////////////////////////////////////////////////////////////////////////
