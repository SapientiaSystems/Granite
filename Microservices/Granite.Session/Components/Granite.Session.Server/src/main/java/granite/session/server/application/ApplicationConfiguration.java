//////////////////////////////////////////////////////////////////////////////
// ApplicationConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.service.event.ISessionEventSender;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.spring.inject.RequestScoped;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableTransactionManagement
public
class ApplicationConfiguration
{
    @Bean
    @RequestScoped
    public ITransactionalSessionCommandService
    sessionCommandService(
        ISessionRepository              repository,
        IOktaLoginTransactionRepository transactionRepository,
        IJwtTokenService                jwtTokenService,
        IRefreshTokenService            refreshTokenService,
        IUserCommandServiceClient       userCommandServiceClient,
        IUserQueryServiceClient         userQueryServiceClient,
        IOktaClient                     oktaClient,
        OktaSettings                    oktaSettings,
        IApplicationLoginGate           loginGate,
        ISessionEventSender             sender,
        ModelMapper                     mapper,
        @Value("${granite.session.max-lifetime:PT24H}") String maxLifetime,
        @Value("${granite.session.token.renewal-margin:PT2M}") String renewalMargin,
        Clock                           clock,
        ApplicationRolesManager         roles,
        IServiceTokenIssuer             serviceTokenIssuer)
    {
        return
            new SessionCommandService(
                repository,
                transactionRepository,
                jwtTokenService,
                refreshTokenService,
                userCommandServiceClient,
                userQueryServiceClient,
                oktaClient,
                oktaSettings,
                loginGate,
                sender,
                mapper,
                Duration.parse(maxLifetime),
                Duration.parse(renewalMargin),
                clock,
                roles,
                serviceTokenIssuer);
    }

    @Bean
    @RequestScoped
    public ITransactionalSessionQueryService
    sessionQueryService(ISessionRepository repository,ModelMapper mapper)
    {
        return new SessionQueryService(repository,mapper);
    }
}

//////////////////////////////////////////////////////////////////////////////
