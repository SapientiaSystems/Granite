//////////////////////////////////////////////////////////////////////////////
// DomainConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;
import strata.server.core.unitofwork.IUnitOfWork;

@Configuration
@EnableTransactionManagement
public
class DomainConfiguration
{
    @Bean
    @RequestScope
    public ISessionRepository
    sessionRepository(IUnitOfWork unitOfWork)
    {
        return new SessionRepository(unitOfWork);
    }

    @Bean
    @RequestScope
    public IOktaLoginTransactionRepository
    oktaLoginTransactionRepository(IUnitOfWork unitOfWork)
    {
        return new OktaLoginTransactionRepository(unitOfWork);
    }
}

//////////////////////////////////////////////////////////////////////////////
