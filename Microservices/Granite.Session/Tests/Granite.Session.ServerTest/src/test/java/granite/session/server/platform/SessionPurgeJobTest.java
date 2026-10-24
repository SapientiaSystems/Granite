//////////////////////////////////////////////////////////////////////////////
// SessionPurgeJobTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.application.TestConfiguration;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.OktaLoginTransaction;
import granite.session.server.domain.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
@ActiveProfiles("local")
public
class SessionPurgeJobTest
{
    @Autowired
    private ApplicationContext context;

    @Autowired
    private SessionPurgeJob job;

    @Autowired
    private ISpringUnitOfWorkManager unitOfWorkManager;

    private TransactionTemplate transaction;

    @BeforeEach
    public void
    setUp()
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
        transaction = new TransactionTemplate(unitOfWorkManager);
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    purgeDeletesOnlyExpiredSessions()
    {
        Long expiredA = save(Instant.now().minus(Duration.ofHours(1)));
        Long expiredB = save(Instant.now().minus(Duration.ofMinutes(5)));
        Long live     = save(Instant.now().plus(Duration.ofHours(1)));

        assertTrue(job.purgeExpired() >= 2);
        assertFalse(exists(expiredA));
        assertFalse(exists(expiredB));
        assertTrue(exists(live));
    }

    @Test
    public void
    purgeHonoursBatchSize()
    {
        job.purgeExpired();

        Long expiredA = save(Instant.now().minus(Duration.ofHours(2)));
        Long expiredB = save(Instant.now().minus(Duration.ofHours(1)));

        SessionPurgeJob batchOfOne =
            new SessionPurgeJob(
                context.getBeanProvider(ISessionRepository.class),
                context.getBeanProvider(IOktaLoginTransactionRepository.class),
                context.getBeanProvider(ISpringUnitOfWorkManager.class),
                Clock.systemUTC(),
                Duration.ofMinutes(15),
                1,
                false);

        assertEquals(1,batchOfOne.purgeExpired());
        assertFalse(exists(expiredA),"the oldest expired session goes first");
        assertTrue(exists(expiredB));
    }

    @Test
    public void
    purgeRunsWithoutAnEnclosingRequest()
    {
        Long expired = save(Instant.now().minus(Duration.ofHours(1)));

        RequestContextHolder.resetRequestAttributes();
        assertTrue(job.purgeExpired() >= 1);
        assertTrue(RequestContextHolder.getRequestAttributes() == null,"job must unbind the scope it created");

        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
        assertFalse(exists(expired));
    }

    @Test
    public void
    purgeDeletesOnlyExpiredOktaTransactions()
    {
        String expired = saveTransaction(Instant.now().minus(Duration.ofMinutes(1)));
        String live    = saveTransaction(Instant.now().plus(Duration.ofMinutes(5)));

        assertTrue(job.purgeExpiredOktaTransactions() >= 1);
        assertFalse(transactionExists(expired));
        assertTrue(transactionExists(live));
    }

    @Test
    public void
    scheduleIsDisabledByProperty()
    {
        assertFalse(job.isAutoStartup());
        assertFalse(job.isRunning());
    }

    private Long
    save(Instant expires)
    {
        return
            transaction.execute(
                status ->
                    context
                        .getBean(ISessionRepository.class)
                        .save(
                            new Session()
                                .setUserId(7000L)
                                .setDeviceId("purge-" + UUID.randomUUID())
                                .setAccessToken("purge-" + UUID.randomUUID())
                                .setExpires(expires))
                        .getSessionId());
    }

    private String
    saveTransaction(Instant expires)
    {
        String hash = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-","");

        transaction.executeWithoutResult(
            status ->
                context
                    .getBean(IOktaLoginTransactionRepository.class)
                    .save(
                        new OktaLoginTransaction()
                            .setHashedTransactionId(hash)
                            .setState("s")
                            .setNonce("n")
                            .setCodeVerifier("v")
                            .setDeviceId("purge-device")
                            .setExpires(expires)));
        return hash;
    }

    private boolean
    transactionExists(String hash)
    {
        return
            Boolean.TRUE.equals(
                transaction.execute(
                    status ->
                        context
                            .getBean(IOktaLoginTransactionRepository.class)
                            .findByHashedTransactionId(hash)
                            .isPresent()));
    }

    private boolean
    exists(Long sessionId)
    {
        return
            Boolean.TRUE.equals(
                transaction.execute(
                    status -> context.getBean(ISessionRepository.class).findById(sessionId).isPresent()));
    }
}

//////////////////////////////////////////////////////////////////////////////
