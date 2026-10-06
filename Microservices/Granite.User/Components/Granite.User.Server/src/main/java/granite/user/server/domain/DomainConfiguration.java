//////////////////////////////////////////////////////////////////////////////
// DomainConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

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
    public IUserRepository
    userRepository(IUnitOfWork unitOfWork)
    {
        return new UserRepository(unitOfWork);
    }
}

//////////////////////////////////////////////////////////////////////////////
