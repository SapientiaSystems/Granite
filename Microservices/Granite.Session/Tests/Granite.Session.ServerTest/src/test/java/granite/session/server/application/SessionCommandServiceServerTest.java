//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceServerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import granite.session.server.domain.Session;
import granite.session.service.requestreply.ISessionCommandService;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginWithOauth2Reply;
import granite.session.service.requestreply.LoginWithOauth2Request;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles("local")
public
class SessionCommandServiceServerTest
    extends granite.session.service.requestreply.SessionCommandServiceTest
{
    @Autowired
    private ISessionCommandService subject;

    @Autowired
    private RSAKey signingKey;

    @Autowired
    private EntityManagerFactory factory;

    @Value("${granite.session.oauth2.providers.test.issuer-uri}")
    private String issuer;

    @Value("${granite.session.oauth2.providers.test.audience}")
    private String audience;

    @BeforeEach
    public void
    setUp()
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    testLoginWritesCreatedOutboxEvent()
    {
        LoginReply reply =
            subject
                .login(newLoginRequest(uniqueUserName(),uniqueDeviceId()))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertCreatedOutboxEvent(reply.getSessionData().getSessionId());
    }

    @Test
    public void
    testLoginWithOauth2WritesCreatedOutboxEvent()
    {
        LoginWithOauth2Reply reply =
            subject
                .loginWithOauth2(
                    new LoginWithOauth2Request()
                        .setProvider(oauth2Provider())
                        .setIdToken(oauth2IdToken())
                        .setDeviceId(uniqueDeviceId()))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertCreatedOutboxEvent(reply.getSessionData().getSessionId());
    }

    @Test
    public void
    testReloginReplacesAndKeepsContinuity()
    {
        String     userName = uniqueUserName();
        String     deviceId = uniqueDeviceId();
        LoginReply first    = login(subject,userName,deviceId);
        LoginReply second   = login(subject,userName,deviceId);

        assertTrue(first.isSuccess(),first.getFailureMessage());
        assertTrue(second.isSuccess(),second.getFailureMessage());
        assertNotEquals(first.getSessionData().getSessionId(),second.getSessionData().getSessionId());
        assertEquals(first.getSessionData().getContinuityId(),second.getSessionData().getContinuityId());
        assertEquals(first.getSessionData().getSessionId(),second.getSessionData().getCopiedFrom());
        assertEquals("REPLACED",closedReasonOf(first.getSessionData().getSessionId()));
        assertEquals(
            second.getSessionData().getSessionId(),
            replacedByOf(first.getSessionData().getSessionId()));
        assertEquals(1L,openSessions(second.getSessionData().getUserId(),deviceId));
        assertFalse(resolveSession(first.getSessionHandle()).isSuccess());
        assertTrue(resolveSession(second.getSessionHandle()).isSuccess());
    }

    @Test
    public void
    testOtherUserDisplacesAndNothingPasses()
    {
        String     deviceId = uniqueDeviceId();
        LoginReply first    = login(subject,uniqueUserName(),deviceId);
        LoginReply second   = login(subject,uniqueUserName(),deviceId);

        assertTrue(first.isSuccess(),first.getFailureMessage());
        assertTrue(second.isSuccess(),second.getFailureMessage());
        assertEquals("DISPLACED",closedReasonOf(first.getSessionData().getSessionId()));
        assertNotEquals(first.getSessionData().getContinuityId(),second.getSessionData().getContinuityId());
        assertNull(second.getSessionData().getCopiedFrom());
        assertEquals(0L,openSessions(first.getSessionData().getUserId(),deviceId));
    }

    @Test
    public void
    testRacingLoginsLeaveExactlyOneActiveSession()
        throws Exception
    {
        ISessionCommandService retrying = new ConcurrencyRetryingSessionCommandService(subject);

        for (int round = 0; round < 6; round++)
        {
            String userName = uniqueUserName();
            String deviceId = uniqueDeviceId();

            if (round % 2 == 1)
                assertTrue(login(subject,userName,deviceId).isSuccess());

            CountDownLatch           start   = new CountDownLatch(1);
            ExecutorService          pool    = Executors.newFixedThreadPool(2);
            List<Future<LoginReply>> futures = new ArrayList<>();

            try
            {
                for (int i = 0; i < 2; i++)
                    futures.add(
                        pool.submit(
                            () ->
                            {
                                RequestContextHolder.setRequestAttributes(new RequestAttributeMap());

                                try
                                {
                                    start.await();
                                    return login(retrying,userName,deviceId);
                                }
                                finally
                                {
                                    RequestContextHolder.resetRequestAttributes();
                                }
                            }));

                start.countDown();

                LoginReply a = futures.get(0).get(60,TimeUnit.SECONDS);
                LoginReply b = futures.get(1).get(60,TimeUnit.SECONDS);

                assertTrue(a.isSuccess(),a.getFailureMessage());
                assertTrue(b.isSuccess(),b.getFailureMessage());
                assertEquals(a.getSessionData().getContinuityId(),b.getSessionData().getContinuityId());
                assertEquals(1L,openSessions(a.getSessionData().getUserId(),deviceId));
            }
            finally
            {
                pool.shutdownNow();
            }
        }
    }

    @Override
    protected ISessionCommandService
    getSubject() { return subject; }

    @Override
    protected String
    oauth2IdToken()
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject("external-" + UUID.randomUUID())
                .claim("email","external@example.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .build();

        try
        {
            SignedJWT signed =
                new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(signingKey.getKeyID())
                        .build(),
                    claims);

            signed.sign(new RSASSASigner(signingKey.toRSAPrivateKey()));
            return signed.serialize();
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException("failed to sign test id token",e);
        }
    }

    private LoginReply
    login(ISessionCommandService service,String userName,String deviceId)
    {
        return
            service
                .login(newLoginRequest(userName,deviceId))
                .toCompletableFuture()
                .join();
    }

    private long
    openSessions(Long userId,String deviceId)
    {
        EntityManager manager = factory.createEntityManager();

        try
        {
            return
                manager
                    .createQuery(
                        "select count(e) from Session e "
                            + "where e.userId = :userId and e.deviceId = :deviceId and e.closedReason is null",
                        Long.class)
                    .setParameter("userId",userId)
                    .setParameter("deviceId",deviceId)
                    .getSingleResult();
        }
        finally
        {
            manager.close();
        }
    }

    private String
    closedReasonOf(Long sessionId)
    {
        return String.valueOf(find(sessionId).getClosedReason());
    }

    private Long
    replacedByOf(Long sessionId)
    {
        return find(sessionId).getReplacedBy();
    }

    private Session
    find(Long sessionId)
    {
        EntityManager manager = factory.createEntityManager();

        try
        {
            return manager.find(Session.class,sessionId);
        }
        finally
        {
            manager.close();
        }
    }

    private void
    assertCreatedOutboxEvent(Long sessionId)
    {
        EntityManager manager = factory.createEntityManager();

        try
        {
            List<String> payloads =
                manager
                    .createQuery(
                        "select e.eventPayload from OutboxEvent e where e.sourceId = :id",
                        String.class)
                    .setParameter("id",sessionId.toString())
                    .getResultList();

            assertTrue(
                payloads.stream().anyMatch(payload -> payload.contains("CREATED")),
                "no CREATED outbox event for session " + sessionId + ": " + payloads);
        }
        finally
        {
            manager.close();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
