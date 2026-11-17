//////////////////////////////////////////////////////////////////////////////
// MvcTestSupport.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import org.mockito.Mockito;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import strata.foundation.core.configuration.IConfiguration;

@Configuration
@EnableWebMvc
public
class MvcTestSupport
{
    @Bean
    public IConfiguration
    configuration()
    {
        IConfiguration configuration = Mockito.mock(IConfiguration.class);

        Mockito.when(configuration.getProperty("granite.cookies.prefix","GRANITE")).thenReturn("GRANITE");
        Mockito.when(configuration.getProperty("granite.session.login.password.enabled","true")).thenReturn("true");
        return configuration;
    }

    @Bean
    public SslBundles
    sslBundles() { return Mockito.mock(SslBundles.class); }
}

//////////////////////////////////////////////////////////////////////////////
