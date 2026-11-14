//////////////////////////////////////////////////////////////////////////////
// GraniteClientCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.configuration;

import granite.client.server.platform.CommandPlatformConfiguration;
import granite.client.server.platform.OutboxEventClientEventSender;
import granite.client.service.event.IClientEventSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import strata.foundation.spring.inject.RequestScoped;
import strata.outbox.core.repository.IOutboxEventRepository;

@Configuration
@Import({CommandPlatformConfiguration.class})
public
class GraniteClientCommandServerConfiguration
    extends AbstractGraniteClientServerConfiguration
{
    @Bean
    @RequestScoped
    public IClientEventSender
    clientEventSender(IOutboxEventRepository repository)
    {
        return new OutboxEventClientEventSender(repository);
    }

    @Override
    protected String
    getServicePathPattern() { return "/client-command-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
