//////////////////////////////////////////////////////////////////////////////
// UserBootstrapper.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public
class UserBootstrapper
    implements ApplicationListener<ContextRefreshedEvent>
{
    private final ObjectProvider<IUserRepository>          repositories;
    private final ObjectProvider<ISpringUnitOfWorkManager> unitOfWorkManagers;
    private final PasswordEncoder                          encoder;
    private final boolean                                  enabled;
    private final String                                   userName;
    private final String                                   password;
    private final Set<String>                              authorities;
    private final Logger                                   logger;
    private boolean                                        done;

    public
    UserBootstrapper(
        ObjectProvider<IUserRepository>          repositories,
        ObjectProvider<ISpringUnitOfWorkManager> unitOfWorkManagers,
        PasswordEncoder                          encoder,
        boolean                                  enabled,
        String                                   userName,
        String                                   password,
        Set<String>                              authorities)
    {
        this.repositories       = Objects.requireNonNull(repositories,"repositories");
        this.unitOfWorkManagers = Objects.requireNonNull(unitOfWorkManagers,"unitOfWorkManagers");
        this.encoder            = Objects.requireNonNull(encoder,"encoder");
        this.enabled            = enabled;
        this.userName           = userName;
        this.password           = password;
        this.authorities        = new LinkedHashSet<>(authorities);
        this.logger             = LogManager.getLogger(UserBootstrapper.class);
        this.done               = false;
    }

    @Override
    public synchronized void
    onApplicationEvent(ContextRefreshedEvent event)
    {
        if (done)
            return;

        done = true;
        bootstrap();
    }

    public boolean
    bootstrap()
    {
        if (!enabled)
            return false;

        if (userName == null || userName.isBlank() || password == null || password.isBlank())
        {
            logger.warn("bootstrap user skipped: user name or password not configured");
            return false;
        }

        boolean bound = RequestContextHolder.getRequestAttributes() == null;

        if (bound)
            RequestContextHolder.setRequestAttributes(new RequestAttributeMap());

        try
        {
            return
                Boolean.TRUE.equals(
                    new TransactionTemplate(unitOfWorkManagers.getObject())
                        .execute(status -> createIfAbsent(repositories.getObject())));
        }
        finally
        {
            if (bound)
                RequestContextHolder.resetRequestAttributes();
        }
    }

    public boolean
    isEnabled() { return enabled; }

    private boolean
    createIfAbsent(IUserRepository repository)
    {
        if (repository.findByUserName(userName).isPresent())
        {
            logger.info("bootstrap user '{}' already exists",userName);
            return false;
        }

        repository.save(
            new User()
                .setUserName(userName)
                .setPassword(encoder.encode(password))
                .setAuthorities(new LinkedHashSet<>(authorities)));
        logger.info("bootstrap user '{}' created with {}",userName,authorities);
        return true;
    }
}

//////////////////////////////////////////////////////////////////////////////
