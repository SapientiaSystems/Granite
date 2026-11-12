//////////////////////////////////////////////////////////////////////////////
// DomainConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

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
    public ISessionRepository
    sessionRepository(IUnitOfWork unitOfWork)
    {
        return new SessionRepository(unitOfWork);
    }

    @Bean
    @RequestScoped
    public IOktaLoginTransactionRepository
    oktaLoginTransactionRepository(IUnitOfWork unitOfWork)
    {
        return new OktaLoginTransactionRepository(unitOfWork);
    }
}

//////////////////////////////////////////////////////////////////////////////
