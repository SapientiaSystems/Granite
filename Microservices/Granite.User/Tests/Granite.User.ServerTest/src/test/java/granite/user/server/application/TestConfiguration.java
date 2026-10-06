//////////////////////////////////////////////////////////////////////////////
// TestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.platform.CommandPlatformConfiguration;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestScope;
import granite.user.server.domain.DomainConfiguration;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:test.properties")
@Import({ApplicationConfiguration.class,DomainConfiguration.class,CommandPlatformConfiguration.class})
public
class TestConfiguration
{
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
            new ApplicationConfigurationProvider("test").get();
    }


}

//////////////////////////////////////////////////////////////////////////////
