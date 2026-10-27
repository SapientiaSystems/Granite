//////////////////////////////////////////////////////////////////////////////
// LoginLifecycleTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.GraniteApplicationRoles;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.SessionClosedReason;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.CredentialKind;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import strata.foundation.core.event.StandardEventType;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class LoginLifecycleTest
{
    private static final Long     USER_ID       = 101L;
    private static final Long     OTHER_USER_ID = 202L;
    private static final String   USER_NAME     = "alice@example.com";
    private static final String   PASSWORD      = "hunter2";
    private static final String   DEVICE_ID     = "device-A";
    private static final Duration MAX_LIFETIME  = Duration.ofHours(24);
    private static final Instant  NOW           = Instant.parse("2026-10-24T12:00:00Z");
    private static final Set<String> LOGIN_ROLES = Set.of("ROLE_USER");

    private ISessionRepository        repository;
    private IJwtTokenService          jwtTokenService;
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
        userCommandServiceClient = Mockito.mock(IUserCommandServiceClient.class);
        userQueryServiceClient   = Mockito.mock(IUserQueryServiceClient.class);
        sender                   = new MockSessionEventSender();
        subject =
            new SessionCommandService(
                repository,
                Mockito.mock(IOktaLoginTransactionRepository.class),
                jwtTokenService,
                new RefreshTokenService(),
                userCommandServiceClient,
                userQueryServiceClient,
                Mockito.mock(IOktaClient.class),
                new OktaSettings(),
                new RequiredAuthoritiesLoginGate(List.of()),
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                MAX_LIFETIME,
                Duration.ofMinutes(2),
                Clock.fixed(NOW,ZoneOffset.UTC),
                GraniteApplicationRoles.create());

        stubSaveAssignsSessionId(500L);
        stubAuthenticate();
        Mockito.when(jwtTokenService.mint(Mockito.any(Session.class))).thenReturn("minted.jwt");
    }

    @Test
    public void
    reloginOnSameDeviceReplaces()
    {
        UUID    cid = UUID.randomUUID();
        Session old =
            session(400L,USER_ID,DEVICE_ID,NOW.minusSeconds(60))
                .setContinuityId(cid)
                .setAuthorities(Set.of("ROLE_ADMIN"))
                .setValue("cart","3");

        onDevice(old);

        LoginReply reply = login();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(cid,reply.getSessionData().getContinuityId());
        assertEquals(Map.of("cart","3"),reply.getSessionData().getValues());
        assertEquals(400L,reply.getSessionData().getCopiedFrom());
        assertEquals(LOGIN_ROLES,reply.getSessionData().getAuthorities());
        assertEquals(SessionClosedReason.REPLACED,old.getClosedReason());
        assertEquals(500L,old.getReplacedBy());
        assertNull(old.getActiveMarker());
        assertEquals(NOW,old.getExpires());

        List<SessionEvent> events = sender.getEvents();

        assertEquals(StandardEventType.UPDATED,events.get(0).getEventType());
        assertEquals("REPLACED",events.get(0).getClosedReason());
        assertEquals(400L,events.get(0).getSourceId());
        assertEquals(StandardEventType.CREATED,events.get(events.size() - 1).getEventType());
    }

    @Test
    public void
    otherUserOnDeviceIsDisplacedAndNothingPasses()
    {
        UUID    cid   = UUID.randomUUID();
        Session other =
            session(401L,OTHER_USER_ID,DEVICE_ID,NOW.minusSeconds(60))
                .setContinuityId(cid)
                .setAuthorities(Set.of("ROLE_ADMIN"))
                .setValue("secret","v");

        onDevice(other);

        LoginReply reply = login();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(SessionClosedReason.DISPLACED,other.getClosedReason());
        assertNull(other.getReplacedBy());
        assertNotNull(reply.getSessionData().getContinuityId());
        assertNotEquals(cid,reply.getSessionData().getContinuityId());
        assertTrue(reply.getSessionData().getValues().isEmpty());
        assertNull(reply.getSessionData().getCopiedFrom());
        assertEquals(LOGIN_ROLES,reply.getSessionData().getAuthorities());
        assertEquals("DISPLACED",sender.getEvents().get(0).getClosedReason());
    }

    @Test
    public void
    firstSessionOnNewDeviceStartsFromLatest()
    {
        UUID    cid    = UUID.randomUUID();
        Session older  =
            session(410L,USER_ID,"device-B",NOW.minus(Duration.ofHours(2)))
                .setValue("v","old");
        Session newer  =
            session(411L,USER_ID,"device-C",NOW.minus(Duration.ofMinutes(5)))
                .setContinuityId(cid)
                .setAuthorities(Set.of("ROLE_ADMIN"))
                .setValue("v","new");

        ofUser(older,newer);

        LoginReply reply = login();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Map.of("v","new"),reply.getSessionData().getValues());
        assertEquals(411L,reply.getSessionData().getCopiedFrom());
        assertNotNull(reply.getSessionData().getContinuityId());
        assertNotEquals(cid,reply.getSessionData().getContinuityId());
        assertEquals(LOGIN_ROLES,reply.getSessionData().getAuthorities());
        assertFalse(newer.isClosed());
        assertFalse(older.isClosed());
        assertNull(newer.getReplacedBy());
    }

    @Test
    public void
    startFromLatestIgnoresClosedSessions()
    {
        Session closed  =
            session(420L,USER_ID,"device-B",NOW.minusSeconds(30))
                .setClosedReason(SessionClosedReason.LOGOUT)
                .setValue("v","closed");
        Session expired =
            session(421L,USER_ID,"device-C",NOW.minusSeconds(40))
                .setExpires(NOW.minusSeconds(1))
                .setValue("v","expired");
        Session open    =
            session(422L,USER_ID,"device-D",NOW.minus(Duration.ofHours(3)))
                .setValue("v","open");

        ofUser(closed,expired,open);

        LoginReply reply = login();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Map.of("v","open"),reply.getSessionData().getValues());
        assertEquals(422L,reply.getSessionData().getCopiedFrom());
    }

    @Test
    public void
    noSessionAnywhereStartsEmpty()
    {
        LoginReply reply = login();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertTrue(reply.getSessionData().getValues().isEmpty());
        assertNull(reply.getSessionData().getCopiedFrom());
        assertNotNull(reply.getSessionData().getContinuityId());
        assertEquals(NOW.plus(MAX_LIFETIME),reply.getSessionData().getExpires());
        assertEquals(1,sender.getEvents().size());
    }

    @Test
    public void
    authoritiesAreNeverCopied()
    {
        Session old =
            session(430L,USER_ID,DEVICE_ID,NOW.minusSeconds(60))
                .setAuthorities(Set.of("ROLE_ADMIN","ROLE_AUDITOR"));

        onDevice(old);

        ArgumentCaptor<Session> minted = ArgumentCaptor.forClass(Session.class);
        LoginReply              reply  = login();

        Mockito.verify(jwtTokenService).mint(minted.capture());
        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(LOGIN_ROLES,minted.getValue().getAuthorities());
        assertEquals(LOGIN_ROLES,reply.getSessionData().getAuthorities());
    }

    @Test
    public void
    expiredOpenSessionOnDeviceIsClosedNotCopied()
    {
        UUID    cid = UUID.randomUUID();
        Session old =
            session(440L,USER_ID,DEVICE_ID,NOW.minus(Duration.ofHours(1)))
                .setContinuityId(cid)
                .setValue("v","stale");

        old.setCreated(NOW.minus(MAX_LIFETIME));
        onDevice(old);

        LoginReply reply = login();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(SessionClosedReason.EXPIRED,old.getClosedReason());
        assertNull(old.getReplacedBy());
        assertTrue(reply.getSessionData().getValues().isEmpty());
        assertNull(reply.getSessionData().getCopiedFrom());
        assertNotEquals(cid,reply.getSessionData().getContinuityId());
    }

    @Test
    public void
    copiedValuesAreIndependent()
    {
        Session old = session(450L,USER_ID,DEVICE_ID,NOW.minusSeconds(60)).setValue("k","v");

        onDevice(old);

        ArgumentCaptor<Session> minted = ArgumentCaptor.forClass(Session.class);
        LoginReply              reply  = login();

        Mockito.verify(jwtTokenService).mint(minted.capture());
        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        minted.getValue().setValue("added","x");
        assertFalse(old.hasValue("added"));
    }

    @Test
    public void
    accessTokenLoginAppliesLifecycle()
    {
        UUID    cid      = UUID.randomUUID();
        String  token    = "existing.jwt";
        Session existing =
            session(460L,USER_ID,DEVICE_ID,NOW.minusSeconds(60))
                .setContinuityId(cid)
                .setAccessToken(token)
                .setValue("k","v");

        onDevice(existing);
        Mockito
            .when(repository.findByAccessToken(token))
            .thenReturn(java.util.Optional.of(existing));
        Mockito
            .when(userQueryServiceClient.findUsersByKey(Mockito.any(FindUsersByKeyRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new FindUsersByKeyReply(invocation.getArgument(0))
                            .setFoundUsers(
                                List.of(
                                    new UserData()
                                        .setUserId(USER_ID)
                                        .setAuthorities(LOGIN_ROLES)))
                            .setSuccess(true)));

        LoginReply reply =
            subject
                .login(
                    new LoginRequest()
                        .setUserName(USER_NAME)
                        .setCredential(token)
                        .setCredentialKind(CredentialKind.ACCESS_TOKEN)
                        .setDeviceId(DEVICE_ID))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(cid,reply.getSessionData().getContinuityId());
        assertEquals(SessionClosedReason.REPLACED,existing.getClosedReason());
        assertEquals(Map.of("k","v"),reply.getSessionData().getValues());
    }

    @Test
    public void
    concurrentWinnerVisibleAfterCloseFailsTheStageForRetry()
    {
        Session winner = session(470L,USER_ID,DEVICE_ID,NOW);

        Mockito
            .when(repository.findOpenByDeviceId(DEVICE_ID))
            .thenReturn(List.of())
            .thenReturn(List.of(winner));

        CompletableFuture<LoginReply> stage =
            subject
                .login(
                    new LoginRequest()
                        .setUserName(USER_NAME)
                        .setCredential(PASSWORD)
                        .setCredentialKind(CredentialKind.PASSWORD)
                        .setDeviceId(DEVICE_ID))
                .toCompletableFuture();

        assertTrue(stage.isCompletedExceptionally());
        assertTrue(new ConflictDetector().isConflict(stage.handle((r,e) -> e).join()));
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));
    }

    private Session
    session(Long id,Long userId,String deviceId,Instant lastActivity)
    {
        Session session =
            new Session()
                .setSessionId(id)
                .setUserId(userId)
                .setDeviceId(deviceId)
                .setExpires(NOW.plus(Duration.ofHours(6)))
                .setAccessToken("token-" + id)
                .setLastActivity(lastActivity);

        session.setCreated(NOW.minus(Duration.ofHours(18)));
        return session;
    }

    private void
    onDevice(Session... sessions)
    {
        Mockito
            .when(repository.findOpenByDeviceId(DEVICE_ID))
            .thenReturn(List.of(sessions));
    }

    private void
    ofUser(Session... sessions)
    {
        Mockito
            .when(repository.findOpenByUserId(USER_ID))
            .thenReturn(List.of(sessions));
    }

    private LoginReply
    login()
    {
        return
            subject
                .login(
                    new LoginRequest()
                        .setUserName(USER_NAME)
                        .setCredential(PASSWORD)
                        .setCredentialKind(CredentialKind.PASSWORD)
                        .setDeviceId(DEVICE_ID))
                .toCompletableFuture()
                .join();
    }

    private void
    stubAuthenticate()
    {
        Mockito
            .when(userCommandServiceClient.authenticateUser(Mockito.any(AuthenticateUserRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new AuthenticateUserReply((AuthenticateUserRequest)invocation.getArgument(0))
                            .setSuccess(true)
                            .setValid(true)
                            .setUser(
                                new UserData()
                                    .setUserId(USER_ID)
                                    .setUserName(USER_NAME)
                                    .setAuthorities(LOGIN_ROLES))));
    }

    private void
    stubSaveAssignsSessionId(long startingId)
    {
        AtomicLong next = new AtomicLong(startingId);

        Mockito
            .when(repository.save(Mockito.any(Session.class)))
            .thenAnswer(
                invocation ->
                {
                    Session s = invocation.getArgument(0);

                    if (s.getSessionId() == null)
                        s.setSessionId(next.getAndIncrement());

                    return s;
                });
    }
}

//////////////////////////////////////////////////////////////////////////////
