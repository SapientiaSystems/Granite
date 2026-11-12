//////////////////////////////////////////////////////////////////////////////
// ApplicationConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.application;

import granite.content.server.domain.IContentDocumentRepository;
import granite.content.service.event.IContentEventSender;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.spring.inject.RequestScoped;

import java.time.Clock;

@Configuration
@EnableTransactionManagement
public
class ApplicationConfiguration
{
    @Bean
    @RequestScoped
    public ITransactionalContentCommandService
    contentCommandService(
        IContentDocumentRepository repository,
        IContentEventSender        sender,
        ModelMapper                mapper,
        Clock                      clock)
    {
        return new ContentCommandService(repository,sender,mapper,clock);
    }

    @Bean
    @RequestScoped
    public ITransactionalContentQueryService
    contentQueryService(IContentDocumentRepository repository,IContentSearchEngine searchEngine,ModelMapper mapper)
    {
        return new ContentQueryService(repository,searchEngine,mapper);
    }
}

//////////////////////////////////////////////////////////////////////////////
