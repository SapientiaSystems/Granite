//////////////////////////////////////////////////////////////////////////////
// ApplicationConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.IUserRepository;
import granite.user.service.event.IUserEventSender;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
@EnableTransactionManagement
public
class ApplicationConfiguration
{
    @Bean
    @RequestScope
    public ITransactionalUserCommandService
    userCommandService(
        IUserRepository  repository,
        IUserEventSender sender,
        ModelMapper            mapper,
        PasswordEncoder        encoder,
        LoginLockoutPolicy     lockoutPolicy)
    {
        return new UserCommandService(repository,sender,mapper,encoder,lockoutPolicy);
    }
    @Bean
    @RequestScope
    public ITransactionalUserQueryService
    userQueryService(
        IUserRepository  repository,
        ModelMapper            mapper)
    {
        return new UserQueryService(repository,mapper);
    }
}

//////////////////////////////////////////////////////////////////////////////
