//////////////////////////////////////////////////////////////////////////////
// PlatformContextTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.session.server.configuration.GraniteSessionCommandServerConfiguration;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ssl.SslBundles;
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
class PlatformContextTestConfiguration
    extends GraniteSessionCommandServerConfiguration
{
    @Override
    @Bean
    @Scope("singleton")
    public IConfiguration
    configuration(Environment environment)
    {
        return new ApplicationConfigurationProvider("test").get();
    }

    @Override
    @Bean
    @Scope("singleton")
    public IUserCommandServiceClient
    userCommandServiceClient(ObjectProvider<SslBundles> sslBundles,Environment environment)
    {
        return Mockito.mock(IUserCommandServiceClient.class);
    }

    @Override
    @Bean
    @Scope("singleton")
    public IUserQueryServiceClient
    userQueryServiceClient(ObjectProvider<SslBundles> sslBundles,Environment environment)
    {
        return Mockito.mock(IUserQueryServiceClient.class);
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
