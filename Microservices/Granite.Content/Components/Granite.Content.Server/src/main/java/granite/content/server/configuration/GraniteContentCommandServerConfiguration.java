//////////////////////////////////////////////////////////////////////////////
// GraniteContentCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.configuration;

import granite.content.server.application.ITransactionalContentCommandService;
import granite.content.server.platform.CommandPlatformConfiguration;
import granite.content.server.platform.OutboxEventContentEventSender;
import granite.content.server.source.ClasspathContentSource;
import granite.content.server.source.ContentSynchronizer;
import granite.content.server.source.IContentSource;
import granite.content.service.event.IContentEventSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.annotation.RequestScope;
import strata.outbox.core.repository.IOutboxEventRepository;

import java.util.List;

@Configuration
@Import({CommandPlatformConfiguration.class})
public
class GraniteContentCommandServerConfiguration
    extends AbstractGraniteContentServerConfiguration
{
    @Bean
    @RequestScope
    public IContentEventSender
    contentEventSender(IOutboxEventRepository repository)
    {
        return new OutboxEventContentEventSender(repository);
    }

    @Bean
    @Scope("singleton")
    public ContentSynchronizer
    contentSynchronizer(ObjectProvider<ITransactionalContentCommandService> commandService)
    {
        return new ContentSynchronizer(contentSources(),commandService);
    }

    protected List<IContentSource>
    contentSources()
    {
        return List.of(new ClasspathContentSource());
    }

    @Override
    protected String
    getServicePathPattern() { return "/content-command-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
