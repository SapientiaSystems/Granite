//////////////////////////////////////////////////////////////////////////////
// PlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.service.event.IUserEventSender;
import granite.user.service.requestreply.IUserCommandService;
import granite.user.service.requestreply.IUserQueryService;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;
import strata.foundation.core.configuration.IConfiguration;
import strata.server.core.unitofwork.IUnitOfWorkSynchronizationManager;
import strata.server.spring.repository.LocalContainerEntityManagerFactoryBeanProvider;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;
import strata.server.spring.unitofwork.JpaUnitOfWork;
import strata.server.spring.unitofwork.JpaUnitOfWorkManager;
import strata.server.spring.unitofwork.SpringUnitOfWorkSynchronizationManager;

@Configuration
@EnableTransactionManagement
public
class QueryPlatformConfiguration
    extends PlatformConfiguration
{
    @Bean
    @RequestScope
    public UserQueryServiceController
    userQueryServiceController(IUserQueryService service)
    {
        return new UserQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
