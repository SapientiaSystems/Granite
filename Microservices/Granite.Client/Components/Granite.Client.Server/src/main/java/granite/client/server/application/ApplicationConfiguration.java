//////////////////////////////////////////////////////////////////////////////
// ApplicationConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import granite.client.server.domain.IClientRepository;
import granite.client.service.event.IClientEventSender;
import granite.foundation.core.security.ApplicationRolesManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.spring.inject.RequestScoped;

import java.time.Clock;
import java.util.Optional;
import java.util.function.Supplier;

@Configuration
@EnableTransactionManagement
public
class ApplicationConfiguration
{
    @Bean
    @RequestScoped
    public ITransactionalClientCommandService
    clientCommandService(
        IClientRepository                                   repository,
        IClientEventSender                                  sender,
        ModelMapper                                         mapper,
        ApplicationRolesManager                             roles,
        Clock                                               clock,
        @Qualifier("clientCallerSubject") Supplier<Optional<String>> callerSubject)
    {
        return new ClientCommandService(repository,sender,mapper,roles,clock,callerSubject);
    }

    @Bean
    @RequestScoped
    public ITransactionalClientQueryService
    clientQueryService(IClientRepository repository,ModelMapper mapper)
    {
        return new ClientQueryService(repository,mapper);
    }
}

//////////////////////////////////////////////////////////////////////////////
