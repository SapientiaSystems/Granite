//////////////////////////////////////////////////////////////////////////////
// PlatformContextTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.application.ApplicationConfiguration;
import granite.session.server.domain.DomainConfiguration;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;

@Configuration
@PropertySource("classpath:test.properties")
@Import(
    {
        ApplicationConfiguration.class,
        DomainConfiguration.class,
        CommandPlatformConfiguration.class
    })
public
class PlatformContextTestConfiguration
{
    @Bean
    public static BeanFactoryPostProcessor
    requestScopeRegistrar()
    {
        return
            factory ->
                factory.registerScope(
                    "request",
                    new org.springframework.web.context.request.RequestScope());
    }

    @Bean
    @Scope("singleton")
    public IConfiguration
    configuration()
    {
        return new ApplicationConfigurationProvider("test").get();
    }

    @Bean
    public IUserCommandServiceClient
    userCommandServiceClient()
    {
        return Mockito.mock(IUserCommandServiceClient.class);
    }

    @Bean
    public IUserQueryServiceClient
    userQueryServiceClient()
    {
        return Mockito.mock(IUserQueryServiceClient.class);
    }
}

//////////////////////////////////////////////////////////////////////////////
