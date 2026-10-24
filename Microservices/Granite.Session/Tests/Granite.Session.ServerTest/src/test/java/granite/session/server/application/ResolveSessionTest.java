//////////////////////////////////////////////////////////////////////////////
// ResolveSessionTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.server.domain.SignInMethod;
import granite.session.service.requestreply.SessionClosedReason;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.ResolveSessionReply;
import granite.session.service.requestreply.ResolveSessionRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.UserData;
import granite.user.service.requestreply.UserKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import strata.foundation.core.event.StandardEventType;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ResolveSessionTest
{
    private static final Long     SESSION_ID     = 900L;
    private static final Long     USER_ID        = 101L;
    private static final String   DEVICE_ID      = "device-A";
    private static final Duration MAX_LIFETIME   = Duration.ofHours(24);
    private static final Duration RENEWAL_MARGIN = Duration.ofMinutes(2);
    private static final String   HANDLE         = "Zk3v0mJb9q1tQnVZ8yJt7m2bQ3o0n9Xc4uV5sWdA1eE";
    private static final String   OLD_JWT        = "old.jwt.token-value";
    private static final String   NEW_JWT        = "new.jwt.token-value";
    private static final String   INVALID        = "invalid session";
    private static final Instant  NOW            = Instant.parse("2026-10-24T12:00:00Z");

    private ISessionRepository        repository;
    private IJwtTokenService          jwtTokenService;
    private IRefreshTokenService      refreshTokenService;
    private IUserCommandServiceClient userCommandServiceClient;
    private IUserQueryServiceClient   userQueryServiceClient;
    private MockSessionEventSender    sender;
    private SessionCommandService     subject;

    @BeforeEach
    public void
    setUp()
    {
        repository               = Mockito.mock(ISessionRepository.class);
        jwtTokenService          = Mockito.mock(IJwtTokenService.class);
        refreshTokenService      = new RefreshTokenService();
        userCommandServiceClient = Mockito.mock(IUserCommandServiceClient.class);
        userQueryServiceClient   = Mockito.mock(IUserQueryServiceClient.class);
        sender                   = new MockSessionEventSender();
        subject =
            new SessionCommandService(
                repository,
                Mockito.mock(IOktaLoginTransactionRepository.class),
                jwtTokenService,
                refreshTokenService,
                userCommandServiceClient,
                userQueryServiceClient,
                Mockito.mock(IOktaClient.class),
                new OktaSettings(),
                new RequiredAuthoritiesLoginGate(List.of()),
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                MAX_LIFETIME,
                RENEWAL_MARGIN,
                Clock.fixed(NOW,ZoneOffset.UTC));

        Mockito
            .when(repository.save(Mockito.any(Session.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito
            .when(jwtTokenService.mint(Mockito.any(Session.class)))
            .thenReturn(NEW_JWT);
    }

    @Test
    public void
    resolveValidHandleReturnsSessionAndAccessToken()
    {
        stub(live());
        tokenExpires(NOW.plus(Duration.ofMinutes(10)));

        ResolveSessionReply reply = resolve(HANDLE);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(OLD_JWT,reply.getAccessToken());
        assertNotNull(reply.getSession());
        assertEquals(SESSION_ID,reply.getSession().getSessionId());
        assertEquals(USER_ID,reply.getSession().getUserId());
        assertEquals(DEVICE_ID,reply.getSession().getDeviceId());
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));
        Mockito.verifyNoInteractions(userQueryServiceClient);
    }

    @Test
    public void
    resolveBlankHandleFails()
    {
        assertRejected(resolve("  "));
        Mockito.verifyNoInteractions(repository,jwtTokenService,userQueryServiceClient);
    }

    @Test
    public void
    resolveNullHandleFails()
    {
        assertRejected(resolve(null));
        Mockito.verifyNoInteractions(repository,jwtTokenService,userQueryServiceClient);
    }

    @Test
    public void
    resolveUnknownHandleFails()
    {
        assertRejected(resolve("unknown-handle"));
        assertNothingWritten();
    }

    @Test
    public void
    resolveClosedSessionFails()
    {
        stub(live().setClosedReason(SessionClosedReason.EXPIRED));
        tokenExpires(NOW.plus(Duration.ofMinutes(10)));

        assertRejected(resolve(HANDLE));
        assertNothingWritten();
    }

    @Test
    public void
    resolveExpiredSessionClosesAsExpired()
    {
        Session session = stub(session(NOW.minus(Duration.ofHours(9)),NOW.minus(Duration.ofMinutes(1))));

        assertRejected(resolve(HANDLE));
        assertClosedAsExpired(session);
    }

    @Test
    public void
    resolvePastMaxLifetimeClosesAsExpired()
    {
        Session session = stub(session(NOW.minus(MAX_LIFETIME),NOW.plus(Duration.ofMinutes(5))));

        tokenExpires(NOW.plus(Duration.ofMinutes(5)));
        assertRejected(resolve(HANDLE));
        assertClosedAsExpired(session);
    }

    @Test
    public void
    resolveInsideMarginRenewsToken()
    {
        Session session = stub(live());

        tokenExpires(NOW.plus(Duration.ofMinutes(1)));
        stubUser(Set.of("ROLE_USER"));

        ResolveSessionReply reply = resolve(HANDLE);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(NEW_JWT,reply.getAccessToken());
        assertEquals(NEW_JWT,session.getAccessToken());
        assertEquals(NOW.plus(Duration.ofHours(7)),session.getExpires());
        assertEquals(NOW.plus(Duration.ofHours(7)),reply.getSession().getExpires());
        Mockito.verify(jwtTokenService).mint(session);
        Mockito.verify(repository).save(session);

        List<SessionEvent> events = sender.getEvents();

        assertEquals(1,events.size());
        assertEquals(StandardEventType.UPDATED,events.get(0).getEventType());
    }

    @Test
    public void
    resolveAtMarginBoundaryRenewsToken()
    {
        stub(live());
        tokenExpires(NOW.plus(RENEWAL_MARGIN));
        stubUser(Set.of("ROLE_USER"));

        ResolveSessionReply reply = resolve(HANDLE);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(NEW_JWT,reply.getAccessToken());
    }

    @Test
    public void
    resolveOutsideMarginDoesNotRenew()
    {
        stub(live());
        tokenExpires(NOW.plus(RENEWAL_MARGIN).plusSeconds(60));

        ResolveSessionReply reply = resolve(HANDLE);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(OLD_JWT,reply.getAccessToken());
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));
        Mockito.verifyNoInteractions(userQueryServiceClient);
    }

    @Test
    public void
    resolveRenewalNeverExceedsMaxLifetime()
    {
        Instant created = NOW.minus(MAX_LIFETIME).plusSeconds(30);
        Session session = stub(session(created,created.plus(MAX_LIFETIME)));

        tokenExpires(NOW.plusSeconds(30));
        stubUser(Set.of("ROLE_USER"));

        ResolveSessionReply reply = resolve(HANDLE);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Mockito.verify(jwtTokenService).mint(session);
        assertEquals(created.plus(MAX_LIFETIME),session.getExpires());
        assertEquals(created.plus(MAX_LIFETIME),reply.getSession().getExpires());
    }

    @Test
    public void
    resolveRenewalRecalculatesAuthoritiesFromUser()
    {
        Session session = stub(live());

        tokenExpires(NOW.plusSeconds(10));
        stubUser(Set.of("ROLE_USER","ROLE_AUDITOR"));

        AtomicReference<Set<String>> minted = mintedAuthorities();
        ResolveSessionReply          reply  = resolve(HANDLE);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Set.of("ROLE_USER","ROLE_AUDITOR"),minted.get());
        assertEquals(Set.of("ROLE_USER","ROLE_AUDITOR"),session.getAuthorities());
        assertEquals(Set.of("ROLE_USER","ROLE_AUDITOR"),reply.getSession().getAuthorities());

        ArgumentCaptor<FindUsersByKeyRequest> request =
            ArgumentCaptor.forClass(FindUsersByKeyRequest.class);

        Mockito.verify(userQueryServiceClient).findUsersByKey(request.capture());
        assertEquals(UserKey.ofUserId(USER_ID),request.getValue().getKey());
    }

    @Test
    public void
    resolveFailsWhenUserMissingAtRenewal()
    {
        stub(live());
        tokenExpires(NOW.plusSeconds(10));
        stubUsers(List.of(),true);

        assertRejected(resolve(HANDLE));
        assertNothingWritten();
    }

    @Test
    public void
    resolveFailsWhenUserLookupFails()
    {
        stub(live());
        tokenExpires(NOW.plusSeconds(10));
        stubUsers(List.of(),false);

        assertRejected(resolve(HANDLE));
        assertNothingWritten();
    }

    @Test
    public void
    resolveFailsGenericallyWhenUserServiceThrows()
    {
        stub(live());
        tokenExpires(NOW.plusSeconds(10));
        Mockito
            .when(userQueryServiceClient.findUsersByKey(Mockito.any(FindUsersByKeyRequest.class)))
            .thenThrow(new IllegalStateException("user service unavailable"));

        assertRejected(resolve(HANDLE));
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));
    }

    @Test
    public void
    resolveRenewsWhenStoredTokenExpirationUnknown()
    {
        Session session = stub(live());

        tokenExpires(null);
        stubUser(Set.of("ROLE_USER"));

        ResolveSessionReply reply = resolve(HANDLE);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(NEW_JWT,reply.getAccessToken());
        Mockito.verify(jwtTokenService).mint(session);
    }

    @Test
    public void
    resolveRecordsLastActivityWhenOlderThanAMinute()
    {
        Session session = stub(live().setLastActivity(NOW.minus(Duration.ofMinutes(2))));

        tokenExpires(NOW.plus(Duration.ofMinutes(10)));

        assertTrue(resolve(HANDLE).isSuccess());
        assertEquals(NOW,session.getLastActivity());
        Mockito.verify(repository).save(session);
        assertTrue(sender.getEvents().isEmpty(),"activity alone fires no event");
    }

    @Test
    public void
    resolveRecordsLastActivityWhenNeverRecorded()
    {
        Session session = stub(live().setLastActivity(null));

        tokenExpires(NOW.plus(Duration.ofMinutes(10)));

        assertTrue(resolve(HANDLE).isSuccess());
        assertEquals(NOW,session.getLastActivity());
        Mockito.verify(repository).save(session);
    }

    @Test
    public void
    resolveDoesNotRecordLastActivityWithinAMinute()
    {
        Instant recent  = NOW.minusSeconds(59);
        Session session = stub(live().setLastActivity(recent));

        tokenExpires(NOW.plus(Duration.ofMinutes(10)));

        assertTrue(resolve(HANDLE).isSuccess());
        assertEquals(recent,session.getLastActivity());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(Session.class));
    }

    @Test
    public void
    resolveNeverLogsHandleOrToken()
    {
        Logger                      logger   = (Logger)LoggerFactory.getLogger("granite.session");
        Level                       previous = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();

        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);

        try
        {
            stub(live());
            tokenExpires(NOW.plusSeconds(10));
            stubUser(Set.of("ROLE_USER"));

            assertTrue(resolve(HANDLE).isSuccess());
            assertRejected(resolve(HANDLE + "-unknown"));
            assertRejected(resolve(" "));
        }
        finally
        {
            logger.detachAppender(appender);
            logger.setLevel(previous);
        }

        assertFalse(appender.list.isEmpty(),"expected resolveSession to log");

        for (ILoggingEvent event : appender.list)
        {
            String message = event.getFormattedMessage();

            assertFalse(message.contains(HANDLE),"handle logged: " + message);
            assertFalse(message.contains(OLD_JWT),"stored token logged: " + message);
            assertFalse(message.contains(NEW_JWT),"minted token logged: " + message);
        }
    }

    @Test
    public void
    oktaRenewalUsesTheSessionsOwnGroups()
    {
        subject = subjectWith(OktaAuthoritiesSource.BOTH);

        stub(live().setSignInMethod(SignInMethod.OKTA).setOktaGroups(Set.of("Granite Admins")));
        tokenExpires(NOW.plus(Duration.ofMinutes(1)));
        stubUser(Set.of("ROLE_USER"));

        AtomicReference<Set<String>> minted = mintedAuthorities();

        assertTrue(resolve(HANDLE).isSuccess());
        assertEquals(Set.of("ROLE_USER","ROLE_ADMIN"),minted.get());
    }

    @Test
    public void
    oktaRenewalUnderGroupsSourceIgnoresUserRoles()
    {
        subject = subjectWith(OktaAuthoritiesSource.OKTA_GROUPS);

        stub(live().setSignInMethod(SignInMethod.OKTA).setOktaGroups(Set.of("Everyone")));
        tokenExpires(NOW.plus(Duration.ofMinutes(1)));
        stubUser(Set.of("ROLE_ADMIN"));

        AtomicReference<Set<String>> minted = mintedAuthorities();

        assertTrue(resolve(HANDLE).isSuccess());
        assertEquals(Set.of(),minted.get());
    }

    @Test
    public void
    passwordRenewalIgnoresTheGroupMapping()
    {
        subject = subjectWith(OktaAuthoritiesSource.OKTA_GROUPS);

        stub(live().setSignInMethod(SignInMethod.PASSWORD));
        tokenExpires(NOW.plus(Duration.ofMinutes(1)));
        stubUser(Set.of("ROLE_AUDITOR"));

        AtomicReference<Set<String>> minted = mintedAuthorities();

        assertTrue(resolve(HANDLE).isSuccess());
        assertEquals(Set.of("ROLE_AUDITOR"),minted.get());
    }

    private SessionCommandService
    subjectWith(OktaAuthoritiesSource source)
    {
        return
            new SessionCommandService(
                repository,
                Mockito.mock(IOktaLoginTransactionRepository.class),
                jwtTokenService,
                refreshTokenService,
                userCommandServiceClient,
                userQueryServiceClient,
                Mockito.mock(IOktaClient.class),
                new OktaSettings()
                    .setAuthoritiesSource(source)
                    .setGroupMapping(java.util.Map.of("Granite Admins","ROLE_ADMIN")),
                new RequiredAuthoritiesLoginGate(List.of()),
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                MAX_LIFETIME,
                RENEWAL_MARGIN,
                Clock.fixed(NOW,ZoneOffset.UTC));
    }

    private Session
    live()
    {
        return session(NOW.minus(Duration.ofHours(1)),NOW.plus(Duration.ofHours(7)));
    }

    private Session
    session(Instant created,Instant expires)
    {
        Session session =
            new Session()
                .setSessionId(SESSION_ID)
                .setUserId(USER_ID)
                .setDeviceId(DEVICE_ID)
                .setExpires(expires)
                .setAccessToken(OLD_JWT)
                .setHashedHandle(refreshTokenService.hash(HANDLE))
                .setLastActivity(NOW.minusSeconds(10))
                .setAuthorities(Set.of("ROLE_USER"));

        session.setCreated(created);
        return session;
    }

    private Session
    stub(Session session)
    {
        Mockito
            .when(repository.findByHashedHandle(refreshTokenService.hash(HANDLE)))
            .thenReturn(Optional.of(session));
        return session;
    }

    private void
    tokenExpires(Instant exp)
    {
        Mockito.when(jwtTokenService.getExpiration(OLD_JWT)).thenReturn(exp);
    }

    private void
    stubUser(Set<String> authorities)
    {
        stubUsers(
            List.of(
                new UserData()
                    .setUserId(USER_ID)
                    .setUserName("alice@example.com")
                    .setAuthorities(authorities)),
            true);
    }

    private void
    stubUsers(List<UserData> users,boolean success)
    {
        Mockito
            .when(userQueryServiceClient.findUsersByKey(Mockito.any(FindUsersByKeyRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new FindUsersByKeyReply(invocation.getArgument(0))
                            .setFoundUsers(users)
                            .setSuccess(success)));
    }

    private AtomicReference<Set<String>>
    mintedAuthorities()
    {
        AtomicReference<Set<String>> minted = new AtomicReference<>();

        Mockito
            .when(jwtTokenService.mint(Mockito.any(Session.class)))
            .thenAnswer(
                invocation ->
                {
                    Session session = invocation.getArgument(0);

                    minted.set(Set.copyOf(session.getAuthorities()));
                    return NEW_JWT;
                });
        return minted;
    }

    private void
    assertClosedAsExpired(Session session)
    {
        assertTrue(session.isClosed());
        assertEquals(SessionClosedReason.EXPIRED,session.getClosedReason());
        Mockito.verify(repository).save(session);
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));

        List<SessionEvent> events = sender.getEvents();

        assertEquals(1,events.size());
        assertEquals(StandardEventType.UPDATED,events.get(0).getEventType());
        assertEquals("EXPIRED",events.get(0).getClosedReason());
        assertNull(session.getActiveMarker());
    }

    private void
    assertRejected(ResolveSessionReply reply)
    {
        assertFalse(reply.isSuccess());
        assertEquals(INVALID,reply.getFailureMessage());
        assertNull(reply.getSession());
        assertNull(reply.getAccessToken());
    }

    private void
    assertNothingWritten()
    {
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(Session.class));
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));
        assertTrue(sender.getEvents().isEmpty(),"expected no events");
    }

    private ResolveSessionReply
    resolve(String handle)
    {
        return
            subject
                .resolveSession(new ResolveSessionRequest().setHandle(handle))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
