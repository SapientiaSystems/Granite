//////////////////////////////////////////////////////////////////////////////
// SessionPurgeJob.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public
class SessionPurgeJob
    implements SmartLifecycle
{
    private final ObjectProvider<ISessionRepository>       repositories;
    private final ObjectProvider<ISpringUnitOfWorkManager> unitOfWorkManagers;
    private final Clock                                    clock;
    private final Duration                                 interval;
    private final int                                      batchSize;
    private final boolean                                  enabled;
    private final Logger                                   logger;
    private ThreadPoolTaskScheduler                        scheduler;

    public
    SessionPurgeJob(
        ObjectProvider<ISessionRepository>       repositories,
        ObjectProvider<ISpringUnitOfWorkManager> unitOfWorkManagers,
        Clock                                    clock,
        Duration                                 interval,
        int                                      batchSize,
        boolean                                  enabled)
    {
        if (batchSize < 1)
            throw new IllegalArgumentException("batchSize must be at least 1");

        this.repositories       = Objects.requireNonNull(repositories,"repositories");
        this.unitOfWorkManagers = Objects.requireNonNull(unitOfWorkManagers,"unitOfWorkManagers");
        this.clock              = Objects.requireNonNull(clock,"clock");
        this.interval           = Objects.requireNonNull(interval,"interval");
        this.batchSize          = batchSize;
        this.enabled            = enabled;
        this.logger             = LogManager.getLogger(SessionPurgeJob.class);
        this.scheduler          = null;
    }

    public int
    purgeExpired()
    {
        boolean bound = RequestContextHolder.getRequestAttributes() == null;

        if (bound)
            RequestContextHolder.setRequestAttributes(new RequestAttributeMap());

        try
        {
            Integer purged =
                new TransactionTemplate(unitOfWorkManagers.getObject())
                    .execute(status -> deleteExpired(repositories.getObject(),clock.instant()));

            logger.info("purged {} expired session(s)",purged);
            return purged != null ? purged : 0;
        }
        finally
        {
            if (bound)
                RequestContextHolder.resetRequestAttributes();
        }
    }

    @Override
    public synchronized void
    start()
    {
        if (scheduler != null)
            return;

        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("session-purge-");
        scheduler.setPoolSize(1);
        scheduler.initialize();
        scheduler.scheduleWithFixedDelay(this::purgeSafely,Instant.now(clock).plus(interval),interval);
    }

    @Override
    public synchronized void
    stop()
    {
        if (scheduler == null)
            return;

        scheduler.shutdown();
        scheduler = null;
    }

    @Override
    public synchronized boolean
    isRunning() { return scheduler != null; }

    @Override
    public boolean
    isAutoStartup() { return enabled; }

    private int
    deleteExpired(ISessionRepository repository,Instant now)
    {
        List<Session> expired = repository.findExpiredBefore(now);
        int           count   = Math.min(batchSize,expired.size());

        for (Session session : expired.subList(0,count))
            repository.delete(session);

        return count;
    }

    private void
    purgeSafely()
    {
        try
        {
            purgeExpired();
        }
        catch (RuntimeException e)
        {
            logger.error("expired session purge failed: {}",e.getMessage(),e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
