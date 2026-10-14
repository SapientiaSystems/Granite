//////////////////////////////////////////////////////////////////////////////
// TestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.user.server.configuration.GraniteUserCommandServerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;

@Configuration
@PropertySource("classpath:test.properties")
public
class TestConfiguration
    extends GraniteUserCommandServerConfiguration
{
    @Override
    @Bean
    @Scope("singleton")
    public IConfiguration
    configuration(Environment environment)
    {
        return new ApplicationConfigurationProvider("test").get();
    }

    @Bean
    @Scope("singleton")
    public ObjectMapper
    objectMapper()
    {
        return new ObjectMapper();
    }
}

//////////////////////////////////////////////////////////////////////////////
