//////////////////////////////////////////////////////////////////////////////
// CommandPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.application.ConcurrencyRetryingSessionCommandService;
import granite.session.server.application.IJwkSetProvider;
import granite.session.server.application.IClientTokenIssuer;
import granite.session.service.requestreply.ISessionCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.spring.inject.RequestScoped;
import strata.outbox.core.repository.RequestScopedRepositoryConfiguration;

@Configuration
@EnableTransactionManagement
@Import({RequestScopedRepositoryConfiguration.class})
public
class CommandPlatformConfiguration
    extends PlatformConfiguration
{
    @Bean
    @RequestScoped
    public SessionCommandServiceController
    sessionCommandServiceController(ISessionCommandService service)
    {
        return new SessionCommandServiceController(new ConcurrencyRetryingSessionCommandService(service));
    }

    @Bean
    public JwkSetController
    jwkSetController(IJwkSetProvider provider)
    {
        return new JwkSetController(provider);
    }

    @Bean
    public ClientTokenController
    clientTokenController(IClientTokenIssuer issuer)
    {
        return new ClientTokenController(issuer);
    }
}

//////////////////////////////////////////////////////////////////////////////
