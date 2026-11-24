//////////////////////////////////////////////////////////////////////////////
// DomainConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.spring.inject.RequestScoped;
import strata.server.core.unitofwork.IUnitOfWork;

@Configuration
@EnableTransactionManagement
public
class DomainConfiguration
{
    @Bean
    @RequestScoped
    public IContentDocumentRepository
    contentRepository(IUnitOfWork unitOfWork)
    {
        return new ContentDocumentRepository(unitOfWork);
    }
}

//////////////////////////////////////////////////////////////////////////////
