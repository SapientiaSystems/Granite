//////////////////////////////////////////////////////////////////////////////
// SessionRepositoryTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.core.unitofwork.IUnitOfWorkSynchronizationManager;
import strata.server.spring.inject.RequestAttributeMap;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public
class SessionRepositoryTest
{
    @Autowired
    private ISessionRepository target;

    @Autowired
    private ISpringUnitOfWorkManager unitOfWorkManager;

    @Autowired
    private IUnitOfWorkSynchronizationManager synchronizer;

    private TransactionTemplate transaction;

    @BeforeAll
    public static void
    init() { System.setProperty("log4j.configuration", "log4j.properties"); }

    @BeforeEach
    public void
    setUp() throws Exception
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
        transaction = new TransactionTemplate(unitOfWorkManager);
    }

    @AfterEach
    public void
    tearDown()
    {
        target = null;
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    testSave() throws Exception
    {
        final AtomicReference<Session> expected = new AtomicReference<>();
        final AtomicReference<Session> actual = new AtomicReference<>();

        transaction.executeWithoutResult(
            status ->
            {
                expected.set(
                    new Session()
                        .setUserId(100L)
                        .setAccessToken("token-save-" + unique())
                        .setDeviceId("device-save-1")
                        .setExpires(Instant.parse("2026-10-07T12:00:00Z")));
                actual.set(target.save(expected.get()));
            });
        assertNotNull(actual.get());
        assertNotNull(actual.get().getPrimaryId());
        assertEquals(expected.get().getAccessToken(),actual.get().getAccessToken());
        assertEquals(expected.get().getUserId(),actual.get().getUserId());
    }

    @Test
    public void
    testFindByAccessToken() throws Exception
    {
        final String token = "token-find-" + unique();
        final AtomicReference<Session> saved = new AtomicReference<>();

        transaction.executeWithoutResult(
            status ->
                saved.set(
                    target.save(
                        new Session()
                            .setUserId(200L)
                            .setAccessToken(token)
                            .setDeviceId("device-find-1")
                            .setExpires(Instant.parse("2026-10-07T12:00:00Z")))));

        Optional<Session> result =
            transaction.execute(status -> target.findByAccessToken(token));

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(saved.get().getPrimaryId(),result.get().getPrimaryId());
        assertEquals(token,result.get().getAccessToken());
    }

    @Test
    public void
    testFindByRefreshTokenHashAndPreviousRefreshTokenHash() throws Exception
    {
        final String current  = "current-" + UUID.randomUUID();
        final String previous = "previous-" + UUID.randomUUID();
        final AtomicReference<Session> saved = new AtomicReference<>();

        transaction.executeWithoutResult(
            status ->
                saved.set(
                    target.save(
                        new Session()
                            .setUserId(250L)
                            .setAccessToken("token-refresh-" + unique())
                            .setRefreshTokenHash(current)
                            .setPreviousRefreshTokenHash(previous)
                            .setDeviceId("device-refresh-1")
                            .setExpires(Instant.parse("2026-10-07T12:00:00Z")))));

        Optional<Session> byCurrent =
            transaction.execute(status -> target.findByRefreshTokenHash(current));
        Optional<Session> byPrevious =
            transaction.execute(status -> target.findByPreviousRefreshTokenHash(previous));
        Optional<Session> byUnknown =
            transaction.execute(status -> target.findByRefreshTokenHash(previous));

        assertTrue(byCurrent.isPresent());
        assertEquals(saved.get().getPrimaryId(),byCurrent.get().getPrimaryId());
        assertEquals(current,byCurrent.get().getRefreshTokenHash());
        assertEquals(previous,byCurrent.get().getPreviousRefreshTokenHash());
        assertTrue(byPrevious.isPresent());
        assertEquals(saved.get().getPrimaryId(),byPrevious.get().getPrimaryId());
        assertFalse(byUnknown.isPresent());
    }

    @Test
    public void
    testFindByUserIdAndDeviceId() throws Exception
    {
        final Long   userId   = 300L + unique();
        final String deviceId = "device-" + unique();
        final AtomicReference<Session> saved = new AtomicReference<>();

        transaction.executeWithoutResult(
            status ->
                saved.set(
                    target.save(
                        new Session()
                            .setUserId(userId)
                            .setAccessToken("token-" + unique())
                            .setDeviceId(deviceId)
                            .setExpires(Instant.parse("2026-10-07T12:00:00Z")))));

        Optional<Session> result =
            transaction.execute(
                status -> target.findByUserIdAndDeviceId(userId,deviceId));

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(saved.get().getPrimaryId(),result.get().getPrimaryId());
    }

    @Test
    public void
    testFindByHashedHandle() throws Exception
    {
        final String  hash         = String.format("%064d",unique());
        final Instant lastActivity = Instant.parse("2026-10-06T12:00:00Z");
        final AtomicReference<Session> saved = new AtomicReference<>();

        transaction.executeWithoutResult(
            status ->
                saved.set(
                    target.save(
                        new Session()
                            .setUserId(500L + unique())
                            .setAccessToken("token-" + unique())
                            .setDeviceId("device-" + unique())
                            .setExpires(Instant.parse("2026-10-07T12:00:00Z"))
                            .setHashedHandle(hash)
                            .setLastActivity(lastActivity)
                            .setClosedReason(SessionClosedReason.EXPIRED))));

        Optional<Session> result =
            transaction.execute(status -> target.findByHashedHandle(hash));

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(saved.get().getPrimaryId(),result.get().getPrimaryId());
        assertEquals(hash,result.get().getHashedHandle());
        assertEquals(lastActivity,result.get().getLastActivity());
        assertEquals(SessionClosedReason.EXPIRED,result.get().getClosedReason());
    }

    @Test
    public void
    testFindByHashedHandleUnknownReturnsEmpty() throws Exception
    {
        Optional<Session> result =
            transaction.execute(status -> target.findByHashedHandle("unknown-" + unique()));

        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    public void
    testFindAllByUserId() throws Exception
    {
        final Long userId = 400L + unique();

        transaction.executeWithoutResult(
            status ->
            {
                target.save(
                    new Session()
                        .setUserId(userId)
                        .setAccessToken("tok-a-" + unique())
                        .setDeviceId("d-a-" + unique())
                        .setExpires(Instant.parse("2026-10-07T12:00:00Z")));
                target.save(
                    new Session()
                        .setUserId(userId)
                        .setAccessToken("tok-b-" + unique())
                        .setDeviceId("d-b-" + unique())
                        .setExpires(Instant.parse("2026-10-07T12:00:00Z")));
            });

        List<Session> result =
            transaction.execute(status -> target.findAllByUserId(userId));

        assertNotNull(result);
        assertEquals(2,result.size());
        assertFalse(
            result.stream().anyMatch(s -> !userId.equals(s.getUserId())),
            "every result should have the requested userId");
    }

    @Autowired
    private jakarta.persistence.EntityManagerFactory entityManagerFactory;

    @Test
    public void
    testUserIdIndexesExist()
    {
        jakarta.persistence.EntityManager em = entityManagerFactory.createEntityManager();
        try
        {
            Number count =
                (Number)em.createNativeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES WHERE UPPER(INDEX_NAME) IN ('IX_SESSION_USER_ID','IX_SESSION_USER_ID_DEVICE_ID','IX_SESSION_PREVIOUS_REFRESH_TOKEN_HASH')")
                    .getSingleResult();

            assertEquals(3,count.intValue());
        }
        finally
        {
            em.close();
        }
    }

    private static long unique = 0L;

    private static synchronized long
    unique() { return ++unique; }
}

//////////////////////////////////////////////////////////////////////////////
