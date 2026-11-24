//////////////////////////////////////////////////////////////////////////////
// DomainConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

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
    public IUserRepository
    userRepository(IUnitOfWork unitOfWork)
    {
        return new UserRepository(unitOfWork);
    }
}

//////////////////////////////////////////////////////////////////////////////
