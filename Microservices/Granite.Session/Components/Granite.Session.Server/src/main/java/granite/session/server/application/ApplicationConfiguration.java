//////////////////////////////////////////////////////////////////////////////
// ApplicationConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.ISessionRepository;
import granite.session.service.event.ISessionEventSender;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableTransactionManagement
public
class ApplicationConfiguration
{
    @Bean
    @RequestScope
    public ITransactionalSessionCommandService
    sessionCommandService(
        ISessionRepository        repository,
        IJwtTokenService          jwtTokenService,
        IUserCommandServiceClient userCommandServiceClient,
        IUserQueryServiceClient   userQueryServiceClient,
        IOauth2IdTokenValidator   oauth2Validator,
        ISessionEventSender       sender,
        ModelMapper               mapper,
        @Value("${granite.session.session-ttl:PT8H}") String sessionTtl,
        Clock                     clock)
    {
        return
            new SessionCommandService(
                repository,
                jwtTokenService,
                userCommandServiceClient,
                userQueryServiceClient,
                oauth2Validator,
                sender,
                mapper,
                Duration.parse(sessionTtl),
                clock);
    }

    @Bean
    @RequestScope
    public ITransactionalSessionQueryService
    sessionQueryService(ISessionRepository repository,ModelMapper mapper)
    {
        return new SessionQueryService(repository,mapper);
    }
}

//////////////////////////////////////////////////////////////////////////////
