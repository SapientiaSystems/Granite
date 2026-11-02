//////////////////////////////////////////////////////////////////////////////
// CloseSessionsTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.CloseSessionsReply;
import granite.session.service.requestreply.CloseSessionsRequest;
import granite.session.service.requestreply.SessionClosedReason;
import granite.session.service.requestreply.SessionData;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class CloseSessionsTest
{
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Instant  NOW = Instant.parse("2026-10-06T12:00:00Z");

    private ISessionRepository     repository;
    private MockSessionEventSender sender;
    private SessionCommandService  subject;

    @BeforeEach
    public void
    setUp()
    {
        repository = Mockito.mock(ISessionRepository.class);
        sender     = new MockSessionEventSender();
        subject    =
            new SessionCommandService(
                repository,
                Mockito.mock(IOktaLoginTransactionRepository.class),
                Mockito.mock(IJwtTokenService.class),
                new RefreshTokenService(),
                Mockito.mock(IUserCommandServiceClient.class),
                Mockito.mock(IUserQueryServiceClient.class),
                Mockito.mock(IOktaClient.class),
                new OktaSettings(),
                new RequiredAuthoritiesLoginGate(List.of()),
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                Duration.ofHours(24),
                Duration.ofMinutes(2),
                Clock.fixed(NOW,ZoneOffset.UTC),
                new GraniteApplicationRolesManagerSupplier().get(),
                null);
    }

    @Test
    public void
    closeSessionsClosesEachAsAdmin()
    {
        Session sessionA = open(901L,1L,"device-A");
        Session sessionB = open(902L,2L,"device-B");

        loaded(Map.of(901L,Optional.of(sessionA),902L,Optional.of(sessionB)));

        CloseSessionsReply reply = closeSessions(Set.of(901L,902L));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Set.of(901L,902L),reply.getRequestedSessionIds());
        assertTrue(reply.getFailedSessionIds().isEmpty());
        assertEquals(2,reply.getClosedSessions().size());

        for (SessionData data : reply.getClosedSessions())
            assertEquals(SessionClosedReason.ADMIN,data.getClosedReason());

        for (Session session : List.of(sessionA,sessionB))
        {
            Mockito.verify(repository).save(session);
            assertEquals(SessionClosedReason.ADMIN,session.getClosedReason());
            assertEquals(NOW,session.getExpires());
        }

        List<SessionEvent> events = sender.getEvents();

        assertEquals(2,events.size(),"expected one close event per session");
        for (SessionEvent event : events)
        {
            assertEquals(StandardEventType.UPDATED,event.getEventType());
            assertEquals("ADMIN",event.getClosedReason());
        }
        Mockito.verify(repository,Mockito.never()).delete(Mockito.any(Session.class));
    }

    @Test
    public void
    closeSessionsReportsUnknownAndAlreadyClosedAsFailed()
    {
        Session open   = open(901L,1L,"device-A");
        Session closed = open(902L,2L,"device-B").close(SessionClosedReason.LOGOUT,NOW.minusSeconds(30));

        Map<Long,Optional<Session>> found = new LinkedHashMap<>();

        found.put(901L,Optional.of(open));
        found.put(902L,Optional.of(closed));
        found.put(903L,Optional.empty());
        loaded(found);

        CloseSessionsReply reply = closeSessions(Set.of(901L,902L,903L));

        assertFalse(reply.isSuccess());
        assertNotNull(reply.getFailureMessage());
        assertEquals(Set.of(901L,902L,903L),reply.getRequestedSessionIds());
        assertEquals(Set.of(902L,903L),reply.getFailedSessionIds());
        assertEquals(1,reply.getClosedSessions().size());
        assertEquals(901L,reply.getClosedSessions().get(0).getSessionId());
        assertEquals(SessionClosedReason.LOGOUT,closed.getClosedReason(),"an already closed session keeps its reason");
        Mockito.verify(repository,Mockito.never()).save(closed);
        assertEquals(1,sender.getEvents().size());
    }

    @Test
    public void
    closeSessionsRejectsEmptySet()
    {
        CloseSessionsReply reply = closeSessions(new TreeSet<>());

        assertFalse(reply.isSuccess());
        assertNotNull(reply.getException());
        Mockito.verify(repository,Mockito.never()).findAllByIdIn(Mockito.any());
        assertTrue(sender.getEvents().isEmpty());
    }

    @Test
    public void
    closeSessionsRejectsMissingSet()
    {
        CloseSessionsReply reply = closeSessions(null);

        assertFalse(reply.isSuccess());
        Mockito.verify(repository,Mockito.never()).findAllByIdIn(Mockito.any());
    }

    private Session
    open(Long sessionId,Long userId,String deviceId)
    {
        return
            new Session()
                .setSessionId(sessionId)
                .setUserId(userId)
                .setDeviceId(deviceId)
                .setExpires(NOW.plus(TTL))
                .setAccessToken("token." + sessionId);
    }

    private void
    loaded(Map<Long,Optional<Session>> found)
    {
        Mockito
            .when(repository.findAllByIdIn(Mockito.anySet()))
            .thenReturn(new LinkedHashMap<>(found));
    }

    private CloseSessionsReply
    closeSessions(Set<Long> sessionIds)
    {
        return
            subject
                .closeSessions(new CloseSessionsRequest().setSessionIds(sessionIds))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
