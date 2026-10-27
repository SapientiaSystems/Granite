//////////////////////////////////////////////////////////////////////////////
// AuthorityRenamer.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public
class AuthorityRenamer
    implements ApplicationListener<ContextRefreshedEvent>
{
    private final ObjectProvider<IUserRepository>          repositories;
    private final ObjectProvider<ISpringUnitOfWorkManager> unitOfWorkManagers;
    private final Map<String,String>                       renames;
    private final ApplicationRolesManager roles;
    private final Logger                                   logger;
    private boolean                                        done;

    public
    AuthorityRenamer(
        ObjectProvider<IUserRepository>          repositories,
        ObjectProvider<ISpringUnitOfWorkManager> unitOfWorkManagers,
        Map<String,String>                       renames,
        ApplicationRolesManager roles)
    {
        this.repositories       = Objects.requireNonNull(repositories,"repositories");
        this.unitOfWorkManagers = Objects.requireNonNull(unitOfWorkManagers,"unitOfWorkManagers");
        this.renames            = new LinkedHashMap<>(Objects.requireNonNull(renames,"renames"));
        this.roles              = Objects.requireNonNull(roles,"roles");
        this.logger             = LogManager.getLogger(AuthorityRenamer.class);
        this.done               = false;
    }

    @Override
    public synchronized void
    onApplicationEvent(ContextRefreshedEvent event)
    {
        if (done)
            return;

        done = true;
        rename();
    }

    public int
    rename()
    {
        boolean bound = RequestContextHolder.getRequestAttributes() == null;

        if (bound)
            RequestContextHolder.setRequestAttributes(new RequestAttributeMap());

        try
        {
            Integer count =
                new TransactionTemplate(unitOfWorkManagers.getObject())
                    .execute(status -> renameAll(repositories.getObject()));

            logger.info("renamed authorities for {} users",count);
            return count;
        }
        finally
        {
            if (bound)
                RequestContextHolder.resetRequestAttributes();
        }
    }

    private int
    renameAll(IUserRepository repository)
    {
        int count = 0;

        for (User user : repository.findAllUsers())
        {
            Set<String> current = user.getAuthorities();

            if (current == null)
                continue;

            Set<String> replaced = new LinkedHashSet<>();

            for (String authority : current)
                replaced.add(renames.getOrDefault(authority,authority));

            Set<String> updated = roles.expand(replaced);

            if (!updated.equals(current))
            {
                user.setAuthorities(updated);
                repository.save(user);
                count++;
            }
        }

        return count;
    }
}

//////////////////////////////////////////////////////////////////////////////
