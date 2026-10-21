//////////////////////////////////////////////////////////////////////////////
// SessionQueryServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.requestreply.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import strata.foundation.core.collection.IMultiMap;
import strata.foundation.core.collection.ListValuedMultiMap;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionQueryServiceTest
{
    private static final Long    USER_ID   = 101L;
    private static final String  DEVICE_ID = "device-A";
    private static final Instant NOW       = Instant.parse("2026-10-06T12:00:00Z");

    private ISessionRepository repository;
    private SessionQueryService subject;

    @BeforeEach
    public void
    setUp()
    {
        repository = Mockito.mock(ISessionRepository.class);
        subject    =
            new SessionQueryService(
                repository,
                new ModelMapper().registerModule(new MappingModule()));
    }

    @Test
    public void
    findSessionsByKeyBySessionIdReturnsMatch()
    {
        Session session = session(700L,USER_ID,"token-700",NOW.plusSeconds(900));

        Mockito.when(repository.findById(700L)).thenReturn(Optional.of(session));

        FindSessionsByKeyReply reply = findByKey(SessionKey.ofSessionId(700L));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(1,reply.getSessions().size());
        assertEquals(700L,reply.getSessions().get(0).getSessionId());
        assertEquals(DEVICE_ID,reply.getSessions().get(0).getDeviceId());
    }

    @Test
    public void
    findSessionsByKeyByUserIdReturnsAllForUser()
    {
        Mockito
            .when(repository.findAllByUserId(USER_ID))
            .thenReturn(
                List.of(
                    session(702L,USER_ID,"token-702",NOW.plusSeconds(900)),
                    session(703L,USER_ID,"token-703",NOW.plusSeconds(900))));

        FindSessionsByKeyReply reply = findByKey(SessionKey.ofUserId(USER_ID));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(2,reply.getSessions().size());
        assertEquals(702L,reply.getSessions().get(0).getSessionId());
        assertEquals(703L,reply.getSessions().get(1).getSessionId());
    }

    @Test
    public void
    findSessionsByKeySetByUserIdsReturnsAggregate()
    {
        IMultiMap<Long,Session> result = new ListValuedMultiMap<>();

        result.put(101L,session(704L,101L,"token-704",NOW.plusSeconds(900)));
        result.put(101L,session(705L,101L,"token-705",NOW.plusSeconds(900)));
        result.put(102L,session(706L,102L,"token-706",NOW.plusSeconds(900)));
        result.putAll(103L,List.of());

        Mockito
            .when(repository.findAllByUserIdIn(Set.of(101L,102L,103L)))
            .thenReturn(result);

        FindSessionsByKeySetReply reply =
            subject
                .findSessionsByKeySet(
                    new FindSessionsByKeySetRequest()
                        .setKeySet(SessionKeySet.ofUserIds(Set.of(101L,102L,103L))))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(3,reply.getSessions().size());
        assertEquals(
            Set.of(704L,705L,706L),
            Set.copyOf(
                reply
                    .getSessions()
                    .stream()
                    .map(data -> data.getSessionId())
                    .toList()));
    }

    @Test
    public void
    findSessionByUserIdAndDeviceIdReturnsMatchOrNull()
    {
        Mockito
            .when(repository.findByUserIdAndDeviceId(USER_ID,DEVICE_ID))
            .thenReturn(
                Optional.of(session(707L,USER_ID,"token-707",NOW.plusSeconds(900))));
        Mockito
            .when(repository.findByUserIdAndDeviceId(USER_ID,"device-B"))
            .thenReturn(Optional.empty());

        FindSessionByUserIdAndDeviceIdReply match =
            findByUserAndDevice(USER_ID,DEVICE_ID);
        FindSessionByUserIdAndDeviceIdReply none =
            findByUserAndDevice(USER_ID,"device-B");

        assertTrue(match.isSuccess(),match.getFailureMessage());
        assertNotNull(match.getSession());
        assertEquals(707L,match.getSession().getSessionId());

        assertTrue(none.isSuccess(),none.getFailureMessage());
        assertNull(none.getSession());
    }

    @Test
    public void
    findReturnsRowEvenWhenExpired()
    {
        Session expired = session(708L,USER_ID,"token-708",NOW.minusSeconds(3600));

        Mockito.when(repository.findById(708L)).thenReturn(Optional.of(expired));

        FindSessionsByKeyReply reply = findByKey(SessionKey.ofSessionId(708L));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(1,reply.getSessions().size());
        assertEquals(NOW.minusSeconds(3600),reply.getSessions().get(0).getExpires());
    }

    private Session
    session(Long id,Long userId,String token,Instant expires)
    {
        return
            new Session()
                .setSessionId(id)
                .setUserId(userId)
                .setDeviceId(DEVICE_ID)
                .setAccessToken(token)
                .setExpires(expires);
    }

    private FindSessionsByKeyReply
    findByKey(SessionKey key)
    {
        return
            subject
                .findSessionsByKey(new FindSessionsByKeyRequest().setKey(key))
                .toCompletableFuture()
                .join();
    }

    private FindSessionByUserIdAndDeviceIdReply
    findByUserAndDevice(Long userId,String deviceId)
    {
        return
            subject
                .findSessionByUserIdAndDeviceId(
                    new FindSessionByUserIdAndDeviceIdRequest()
                        .setUserId(userId)
                        .setDeviceId(deviceId))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
