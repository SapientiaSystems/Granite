//////////////////////////////////////////////////////////////////////////////
// PlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.service.event.IUserEventSender;
import granite.user.service.requestreply.IUserCommandService;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;
import strata.foundation.core.configuration.IConfiguration;
import strata.outbox.core.repository.RequestScopedRepositoryConfiguration;
import strata.server.core.unitofwork.IUnitOfWorkSynchronizationManager;
import strata.server.spring.repository.LocalContainerEntityManagerFactoryBeanProvider;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;
import strata.server.spring.unitofwork.JpaUnitOfWork;
import strata.server.spring.unitofwork.JpaUnitOfWorkManager;
import strata.server.spring.unitofwork.SpringUnitOfWorkSynchronizationManager;

@Configuration
@EnableTransactionManagement
@Import({RequestScopedRepositoryConfiguration.class})
public
class CommandPlatformConfiguration
    extends PlatformConfiguration
{

    @Bean
    @RequestScope
    public UserCommandServiceController
    userCommandServiceController(IUserCommandService service)
    {
        return new UserCommandServiceController(service);
    }

    @Bean
    @RequestScope
    public IUserEventSender
    userEventSender(IUnitOfWorkSynchronizationManager manager)
    {
        return
            new OnCommitUserEventSender(new MockUserEventSender(),manager);
    }
}

//////////////////////////////////////////////////////////////////////////////
