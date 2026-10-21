//////////////////////////////////////////////////////////////////////////////
// GraniteUserCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.configuration;

import granite.user.server.domain.IUserRepository;
import granite.user.server.platform.CommandPlatformConfiguration;
import granite.user.server.platform.OutboxEventUserEventSender;
import granite.user.server.platform.UserBootstrapper;
import granite.user.service.event.IUserEventSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.annotation.RequestScope;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.util.LinkedHashSet;
import java.util.List;

@Configuration
@Import({CommandPlatformConfiguration.class})
public
class GraniteUserCommandServerConfiguration
    extends AbstractGraniteUserServerConfiguration
{
    @Bean
    @RequestScope
    public IUserEventSender
    userEventSender(IOutboxEventRepository repository)
    {
        return new OutboxEventUserEventSender(repository);
    }

    @Bean
    @Scope("singleton")
    public UserBootstrapper
    userBootstrapper(
        ObjectProvider<IUserRepository>          repositories,
        ObjectProvider<ISpringUnitOfWorkManager> unitOfWorkManagers,
        PasswordEncoder                          encoder,
        Environment                              environment)
    {
        boolean enabled = environment.getProperty("granite.user.bootstrap.enabled",Boolean.class,false);

        if (enabled &&
            (!environment.acceptsProfiles(Profiles.of("local","development")) ||
             environment.acceptsProfiles(Profiles.of("test","production"))))
            throw
                new IllegalStateException(
                    "granite.user.bootstrap.enabled=true is permitted only in the 'local' and " +
                    "'development' environments");

        return
            new UserBootstrapper(
                repositories,
                unitOfWorkManagers,
                encoder,
                enabled,
                environment.getProperty("granite.user.bootstrap.user-name",""),
                environment.getProperty("granite.user.bootstrap.password",""),
                new LinkedHashSet<>(
                    Binder
                        .get(environment)
                        .bind("granite.user.bootstrap.authorities",Bindable.listOf(String.class))
                        .orElse(List.of("ROLE_USER"))));
    }

    @Override
    protected String
    getServicePathPattern() { return "/user-command-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
