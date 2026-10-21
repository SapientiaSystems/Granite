//////////////////////////////////////////////////////////////////////////////
// LogoutAllForUserTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.platform.MockSessionEventSender;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.LogoutAllForUserReply;
import granite.session.service.requestreply.LogoutAllForUserRequest;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class LogoutAllForUserTest
{
    private static final Long     USER_ID    = 101L;
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
                clock);
    }

    @Test
    public void
    logoutAllForUserDestroysEverySessionForUser()
    {
        Session sessionA =
            new Session()
                .setSessionId(801L)
                .setUserId(USER_ID)
                .setDeviceId("device-A")
                .setExpires(NOW.plus(TTL))
                .setAccessToken("token.A");
        Session sessionB =
            new Session()
                .setSessionId(802L)
                .setUserId(USER_ID)
                .setDeviceId("device-B")
                .setExpires(NOW.plus(TTL))
                .setAccessToken("token.B");
        Session sessionC =
            new Session()
                .setSessionId(803L)
                .setUserId(USER_ID)
                .setDeviceId("device-C")
                .setExpires(NOW.plus(TTL))
                .setAccessToken("token.C");

        Mockito
            .when(repository.findAllByUserId(USER_ID))
            .thenReturn(List.of(sessionA,sessionB,sessionC));

        LogoutAllForUserReply reply = logoutAllForUser(USER_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getDestroyed());
        assertEquals(Set.of(801L,802L,803L),reply.getDestroyed());
        Mockito.verify(repository).delete(sessionA);
        Mockito.verify(repository).delete(sessionB);
        Mockito.verify(repository).delete(sessionC);

        List<SessionEvent> events = sender.getEvents();

        assertEquals(3,events.size(),"expected one SessionDestroyed event per session");
        for (SessionEvent event : events)
            assertEquals(StandardEventType.DESTROYED,event.getEventType());
        assertEquals(
            Set.of(801L,802L,803L),
            Set.of(events.get(0).getSourceId(),events.get(1).getSourceId(),events.get(2).getSourceId()));
    }

    @Test
    public void
    logoutAllForUserWhenNoneExistReturnsEmptyDestroyedSet()
    {
        Mockito
            .when(repository.findAllByUserId(USER_ID))
            .thenReturn(Collections.emptyList());

        LogoutAllForUserReply reply = logoutAllForUser(USER_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getDestroyed());
        assertTrue(reply.getDestroyed().isEmpty(),"expected destroyed set to be empty");
        Mockito.verify(repository,Mockito.never()).delete(Mockito.any(Session.class));
        assertTrue(sender.getEvents().isEmpty(),"expected no events");
    }

    private LogoutAllForUserReply
    logoutAllForUser(Long userId)
    {
        return
            subject
                .logoutAllForUser(new LogoutAllForUserRequest().setUserId(userId))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
