//////////////////////////////////////////////////////////////////////////////
// RefreshTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.platform.MockSessionEventSender;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.RefreshReply;
import granite.session.service.requestreply.RefreshRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class RefreshTest
{
    private static final Long     SESSION_ID    = 900L;
    private static final Long     USER_ID       = 101L;
    private static final String   DEVICE_ID     = "device-A";
    private static final Duration TTL           = Duration.ofMinutes(15);
    private static final Duration MAX_LIFETIME  = Duration.ofHours(24);
    private static final String   OLD_JWT       = "old.jwt.token";
    private static final String   NEW_JWT       = "new.jwt.token";
    private static final String   REFRESH_TOKEN = "current-refresh-token";
    private static final String   PRIOR_TOKEN   = "prior-refresh-token";
    private static final Instant  NOW           = Instant.parse("2026-10-06T12:00:00Z");

    private ISessionRepository        repository;
    private IJwtTokenService          jwtTokenService;
    private IRefreshTokenService      refreshTokenService;
    private IUserCommandServiceClient userCommandServiceClient;
    private IUserQueryServiceClient   userQueryServiceClient;
    private IOauth2IdTokenValidator   oauth2Validator;
    private MockSessionEventSender    sender;
    private Clock                     clock;
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
        oauth2Validator          = Mockito.mock(IOauth2IdTokenValidator.class);
        sender                   = new MockSessionEventSender();
        clock                    = Clock.fixed(NOW,ZoneOffset.UTC);
        subject =
            new SessionCommandService(
                repository,
                jwtTokenService,
                refreshTokenService,
                userCommandServiceClient,
                userQueryServiceClient,
                oauth2Validator,
                new RequiredAuthoritiesLoginGate(List.of()),
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                TTL,
                MAX_LIFETIME,
                clock);

        Mockito
            .when(repository.save(Mockito.any(Session.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito
            .when(jwtTokenService.mint(Mockito.any(Session.class)))
            .thenReturn(NEW_JWT);
    }

    @Test
    public void
    refreshIssuesNewJwtAndUpdatesExpires()
    {
        Session existing = stubCurrent(session(NOW.minus(Duration.ofHours(1)),NOW.plus(Duration.ofMinutes(5))));

        RefreshReply reply = refresh(REFRESH_TOKEN);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(NEW_JWT,reply.getAccessToken());
        assertNotNull(reply.getSessionData());
        assertEquals(SESSION_ID,reply.getSessionData().getSessionId());
        assertEquals(USER_ID,reply.getSessionData().getUserId());
        assertEquals(NOW.plus(TTL),reply.getSessionData().getExpires());

        ArgumentCaptor<Session> mintCaptor = ArgumentCaptor.forClass(Session.class);

        Mockito.verify(jwtTokenService).mint(mintCaptor.capture());
        assertEquals(
            NOW.plus(TTL),
            mintCaptor.getValue().getExpires(),
            "jwt should be minted using the refreshed expires");
        Mockito.verify(repository).save(existing);
    }

    @Test
    public void
    refreshRotatesRefreshToken()
    {
        Session existing = stubCurrent(session(NOW.minus(Duration.ofHours(1)),NOW.plus(Duration.ofMinutes(5))));

        RefreshReply reply = refresh(REFRESH_TOKEN);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getRefreshToken());
        assertNotEquals(REFRESH_TOKEN,reply.getRefreshToken());
        assertEquals(refreshTokenService.hash(reply.getRefreshToken()),existing.getRefreshTokenHash());
        assertEquals(refreshTokenService.hash(REFRESH_TOKEN),existing.getPreviousRefreshTokenHash());
    }

    @Test
    public void
    refreshFiresSessionUpdatedEvent()
    {
        stubCurrent(session(NOW.minus(Duration.ofHours(1)),NOW.plus(Duration.ofMinutes(5))));

        RefreshReply reply = refresh(REFRESH_TOKEN);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());

        List<SessionEvent> events = sender.getEvents();

        assertEquals(1,events.size(),"expected exactly one SessionUpdated event");
        assertEquals(StandardEventType.UPDATED,events.get(0).getEventType());
        assertEquals(SESSION_ID,events.get(0).getSourceId());
    }

    @Test
    public void
    refreshCapsExpiresAtMaxLifetime()
    {
        Instant created = NOW.minus(MAX_LIFETIME).plus(Duration.ofMinutes(5));

        stubCurrent(session(created,NOW.plus(Duration.ofMinutes(1))));

        RefreshReply reply = refresh(REFRESH_TOKEN);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(created.plus(MAX_LIFETIME),reply.getSessionData().getExpires());
    }

    @Test
    public void
    refreshRejectsSessionPastMaxLifetime()
    {
        stubCurrent(session(NOW.minus(MAX_LIFETIME),NOW.plus(Duration.ofMinutes(5))));

        RefreshReply reply = refresh(REFRESH_TOKEN);

        assertRejected(reply,"session expired");
    }

    @Test
    public void
    refreshRejectsExpiredSession()
    {
        stubCurrent(session(NOW.minus(Duration.ofHours(1)),NOW.minus(Duration.ofMinutes(1))));

        RefreshReply reply = refresh(REFRESH_TOKEN);

        assertRejected(reply,"session expired");
    }

    @Test
    public void
    refreshWithUnknownTokenReturnsSuccessFalse()
    {
        RefreshReply reply = refresh("unknown-refresh-token");

        assertRejected(reply,"invalid refresh token");
        Mockito
            .verify(repository,Mockito.never())
            .delete(Mockito.any(Session.class));
    }

    @Test
    public void
    refreshWithReplayedTokenRevokesSession()
    {
        Session existing = session(NOW.minus(Duration.ofHours(1)),NOW.plus(Duration.ofMinutes(5)));

        Mockito
            .when(repository.findByPreviousRefreshTokenHash(refreshTokenService.hash(PRIOR_TOKEN)))
            .thenReturn(Optional.of(existing));

        RefreshReply reply = refresh(PRIOR_TOKEN);

        assertFalse(reply.isSuccess());
        assertTrue(
            reply.getFailureMessage().contains("reused"),
            "expected failure message to mention reuse but was: " + reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        assertNull(reply.getRefreshToken());
        Mockito.verify(repository).delete(existing);
        Mockito
            .verify(jwtTokenService,Mockito.never())
            .mint(Mockito.any(Session.class));

        List<SessionEvent> events = sender.getEvents();

        assertEquals(1,events.size(),"expected exactly one SessionDestroyed event");
        assertEquals(StandardEventType.DESTROYED,events.get(0).getEventType());
        assertEquals(SESSION_ID,events.get(0).getSourceId());
    }

    @Test
    public void
    refreshWithBlankTokenReturnsSuccessFalse()
    {
        RefreshReply reply = refresh(" ");

        assertFalse(reply.isSuccess());
        assertNotNull(reply.getException());
        Mockito
            .verifyNoInteractions(repository,jwtTokenService);
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
                .setRefreshTokenHash(refreshTokenService.hash(REFRESH_TOKEN))
                .setPreviousRefreshTokenHash(refreshTokenService.hash(PRIOR_TOKEN));

        session.setCreated(created);
        return session;
    }

    private Session
    stubCurrent(Session session)
    {
        Mockito
            .when(repository.findByRefreshTokenHash(refreshTokenService.hash(REFRESH_TOKEN)))
            .thenReturn(Optional.of(session));
        return session;
    }

    private void
    assertRejected(RefreshReply reply,String expectedMessage)
    {
        assertFalse(reply.isSuccess());
        assertNotNull(reply.getFailureMessage());
        assertTrue(
            reply.getFailureMessage().contains(expectedMessage),
            "expected failure message to mention '" + expectedMessage + "' but was: " + reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        assertNull(reply.getRefreshToken());
        assertNull(reply.getSessionData());
        Mockito
            .verify(jwtTokenService,Mockito.never())
            .mint(Mockito.any(Session.class));
        Mockito
            .verify(repository,Mockito.never())
            .save(Mockito.any(Session.class));
        assertTrue(sender.getEvents().isEmpty(),"expected no events");
    }

    private RefreshReply
    refresh(String refreshToken)
    {
        return
            subject
                .refresh(new RefreshRequest().setRefreshToken(refreshToken))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
