//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceServerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.ISessionCommandService;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.session.service.requestreply.LogoutReply;
import granite.session.service.requestreply.LogoutRequest;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private FakeOkta okta;

    @Autowired
    private EntityManagerFactory factory;

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
    testCompleteOktaLoginEndToEnd()
    {
        String                 deviceId = uniqueDeviceId();
        String                 sub      = "00u" + UUID.randomUUID().toString().replace("-","");
        StartOktaLoginReply    start    = startOkta(deviceId,"/users?tab=1");
        Map<String,String>     query    = query(start.getAuthorizeUrl());

        assertTrue(start.isSuccess(),start.getFailureMessage());
        assertNotNull(start.getTransactionId());
        assertEquals("S256",query.get("code_challenge_method"));
        assertFalse(query.containsKey("prompt"));
        okta.stubTokenReturning(okta.idToken(sub,query.get("nonce")));

        CompleteOktaLoginReply reply = completeOkta(start.getTransactionId(),query.get("state"),deviceId);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getSessionHandle());
        assertEquals("/users?tab=1",reply.getReturnTo());
        assertEquals(sub + "@example.com",reply.getUserName());
        assertEquals(Set.of("ROLE_Admin"),reply.getSessionData().getAuthorities());
        assertCreatedOutboxEvent(reply.getSessionData().getSessionId());
        assertEquals("OKTA",find(reply.getSessionData().getSessionId()).getSignInMethod().name());
        assertTrue(resolveSession(reply.getSessionHandle()).isSuccess());

        CompleteOktaLoginReply reused = completeOkta(start.getTransactionId(),query.get("state"),deviceId);

        assertFalse(reused.isSuccess());
        assertEquals("invalid credentials",reused.getFailureMessage());

        StartOktaLoginReply again = startOkta(deviceId,"/");

        assertEquals("login",query(again.getAuthorizeUrl()).get("prompt"));

        LogoutReply logout =
            subject
                .logout(new LogoutRequest().setSessionId(reply.getSessionData().getSessionId()))
                .toCompletableFuture()
                .join();

        assertTrue(logout.isSuccess(),logout.getFailureMessage());
        assertTrue(logout.getEndSessionUrl().startsWith(okta.url(FakeOkta.LOGOUT_PATH)));
        assertTrue(logout.getEndSessionUrl().contains("id_token_hint="));
        assertNull(find(reply.getSessionData().getSessionId()).getOktaIdToken());
    }

    @Test
    public void
    testOktaReloginReplacesAndKeepsContinuity()
    {
        String                 deviceId = uniqueDeviceId();
        String                 sub      = "00u" + UUID.randomUUID().toString().replace("-","");
        CompleteOktaLoginReply first    = oktaLogin(sub,deviceId);
        CompleteOktaLoginReply second   = oktaLogin(sub,deviceId);

        assertTrue(first.isSuccess(),first.getFailureMessage());
        assertTrue(second.isSuccess(),second.getFailureMessage());
        assertEquals(first.getSessionData().getUserId(),second.getSessionData().getUserId());
        assertEquals(first.getSessionData().getContinuityId(),second.getSessionData().getContinuityId());
        assertEquals("REPLACED",closedReasonOf(first.getSessionData().getSessionId()));
        assertEquals(1L,openSessions(second.getSessionData().getUserId(),deviceId));
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

    private CompleteOktaLoginReply
    oktaLogin(String sub,String deviceId)
    {
        StartOktaLoginReply start = startOkta(deviceId,"/");
        Map<String,String>  query = query(start.getAuthorizeUrl());

        okta.stubTokenReturning(okta.idToken(sub,query.get("nonce")));
        return completeOkta(start.getTransactionId(),query.get("state"),deviceId);
    }

    private StartOktaLoginReply
    startOkta(String deviceId,String returnTo)
    {
        return
            subject
                .startOktaLogin(new StartOktaLoginRequest().setDeviceId(deviceId).setReturnTo(returnTo))
                .toCompletableFuture()
                .join();
    }

    private CompleteOktaLoginReply
    completeOkta(String transactionId,String state,String deviceId)
    {
        return
            subject
                .completeOktaLogin(
                    new CompleteOktaLoginRequest()
                        .setCode("code-" + UUID.randomUUID())
                        .setState(state)
                        .setTransactionId(transactionId)
                        .setDeviceId(deviceId))
                .toCompletableFuture()
                .join();
    }

    private Map<String,String>
    query(String url)
    {
        return UriComponentsBuilder.fromUriString(url).build().getQueryParams().toSingleValueMap();
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
