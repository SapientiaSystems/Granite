//////////////////////////////////////////////////////////////////////////////
// PlatformContextTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.session.server.application.IServiceTokenProvider;
import granite.session.server.configuration.GraniteSessionCommandServerConfiguration;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ssl.SslBundles;
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
class PlatformContextTestConfiguration
    extends GraniteSessionCommandServerConfiguration
{
    @Override
    @Bean
    @SingletonScoped
    public IConfiguration
    configuration(Environment environment)
    {
        return new ApplicationConfigurationProvider("application-local").get();
    }

    @Override
    @Bean
    @SingletonScoped
    public IUserCommandServiceClient
    userCommandServiceClient(
        ObjectProvider<SslBundles> sslBundles,
        Environment                environment,
        IServiceTokenProvider      serviceTokenProvider)
    {
        return Mockito.mock(IUserCommandServiceClient.class);
    }

    @Override
    @Bean
    @SingletonScoped
    public IUserQueryServiceClient
    userQueryServiceClient(
        ObjectProvider<SslBundles> sslBundles,
        Environment                environment,
        IServiceTokenProvider      serviceTokenProvider)
    {
        return Mockito.mock(IUserQueryServiceClient.class);
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
