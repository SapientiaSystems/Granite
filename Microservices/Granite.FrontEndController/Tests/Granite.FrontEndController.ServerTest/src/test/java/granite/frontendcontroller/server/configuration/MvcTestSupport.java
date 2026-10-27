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
    configuration() { return Mockito.mock(IConfiguration.class); }

    @Bean
    public SslBundles
    sslBundles() { return Mockito.mock(SslBundles.class); }
}

//////////////////////////////////////////////////////////////////////////////
