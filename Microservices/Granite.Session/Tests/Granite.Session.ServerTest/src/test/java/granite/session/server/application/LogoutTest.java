//////////////////////////////////////////////////////////////////////////////
// LogoutTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.platform.MockSessionEventSender;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.LogoutReply;
import granite.session.service.requestreply.LogoutRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class LogoutTest
{
    private static final Long     SESSION_ID = 700L;
    private static final Long     USER_ID    = 101L;
    private static final String   DEVICE_ID  = "device-A";
    private static final Duration TTL        = Duration.ofMinutes(15);
    private static final Instant  NOW        = Instant.parse("2026-10-06T12:00:00Z");

    private ISessionRepository        repository;
    private IJwtTokenService          jwtTokenService;
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
        userCommandServiceClient = Mockito.mock(IUserCommandServiceClient.class);
        userQueryServiceClient   = Mockito.mock(IUserQueryServiceClient.class);
        oauth2Validator          = Mockito.mock(IOauth2IdTokenValidator.class);
        sender                   = new MockSessionEventSender();
        clock                    = Clock.fixed(NOW,ZoneOffset.UTC);
        subject =
            new SessionCommandService(
                repository,
                jwtTokenService,
                new RefreshTokenService(),
                userCommandServiceClient,
                userQueryServiceClient,
                oauth2Validator,
                new RequiredAuthoritiesLoginGate(List.of()),
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                TTL,
                Duration.ofHours(24),
                Duration.ofMinutes(2),
                clock);
    }

    @Test
    public void
    logoutDestroysSessionAndFiresEvent()
    {
        Session existing =
            new Session()
                .setSessionId(SESSION_ID)
                .setUserId(USER_ID)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(TTL))
                .setAccessToken("some.jwt.token");

        Mockito
            .when(repository.findById(SESSION_ID))
            .thenReturn(Optional.of(existing));

        LogoutReply reply = logout(SESSION_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Mockito.verify(repository).delete(existing);

        List<SessionEvent> events = sender.getEvents();

        assertEquals(1,events.size(),"expected exactly one SessionDestroyed event");
        assertEquals(StandardEventType.DESTROYED,events.get(0).getEventType());
        assertEquals(SESSION_ID,events.get(0).getSourceId());
    }

    @Test
    public void
    logoutOfUnknownSessionReturnsSuccessFalse()
    {
        Mockito
            .when(repository.findById(SESSION_ID))
            .thenReturn(Optional.empty());

        LogoutReply reply = logout(SESSION_ID);

        assertFalse(reply.isSuccess());
        assertNotNull(reply.getFailureMessage());
        assertTrue(
            reply.getFailureMessage().contains("session not found"),
            "expected failure message to mention 'session not found' but was: " + reply.getFailureMessage());
        Mockito.verify(repository,Mockito.never()).delete(Mockito.any(Session.class));
        assertTrue(sender.getEvents().isEmpty(),"expected no events");
    }

    private LogoutReply
    logout(Long sessionId)
    {
        return
            subject
                .logout(new LogoutRequest().setSessionId(sessionId))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
