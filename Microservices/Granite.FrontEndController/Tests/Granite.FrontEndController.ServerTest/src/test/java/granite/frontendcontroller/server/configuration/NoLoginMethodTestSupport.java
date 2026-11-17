//////////////////////////////////////////////////////////////////////////////
// NoLoginMethodTestSupport.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;
import strata.foundation.core.configuration.IConfiguration;

@Configuration
public
class NoLoginMethodTestSupport
    extends MvcTestSupport
{
    @Override
    public IConfiguration
    configuration()
    {
        IConfiguration configuration = Mockito.mock(IConfiguration.class);

        Mockito.when(configuration.getProperty("granite.cookies.prefix","GRANITE")).thenReturn("GRANITE");
        Mockito.when(configuration.getProperty("granite.session.login.password.enabled","true")).thenReturn("false");
        Mockito.when(configuration.getProperty("granite.session.login.okta.enabled","false")).thenReturn("false");
        return configuration;
    }
}

//////////////////////////////////////////////////////////////////////////////
