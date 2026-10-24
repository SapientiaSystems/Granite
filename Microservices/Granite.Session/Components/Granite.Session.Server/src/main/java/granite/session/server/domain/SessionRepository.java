//////////////////////////////////////////////////////////////////////////////
// SessionRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import strata.foundation.core.collection.IMultiMap;
import strata.foundation.core.collection.ListValuedMultiMap;
import strata.server.core.repository.AbstractRepository;
import strata.server.core.unitofwork.IUnitOfWork;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public
class SessionRepository
    extends AbstractRepository<Long,Session>
    implements ISessionRepository
{
    public
    SessionRepository(IUnitOfWork uow)
    {
        super(Session.class,"primaryId",uow);
        getUnitOfWork()
            .registerQuery(
                "findAllByIdIn",
                "select e from Session e where e.primaryId IN :sessionIds")
            .registerQuery(
                "findByAccessToken",
                "select e from Session e where e.accessToken = :accessToken")
            .registerQuery(
                "findByRefreshTokenHash",
                "select e from Session e where e.refreshTokenHash = :hash")
            .registerQuery(
                "findExpiredBefore",
                "select e from Session e where e.expires < :time order by e.expires")
            .registerQuery(
                "findByPreviousRefreshTokenHash",
                "select e from Session e where e.previousRefreshTokenHash = :hash")
            .registerQuery(
                "findByHashedHandle",
                "select e from Session e where e.hashedHandle = :hash")
            .registerQuery(
                "findAllByUserId",
                "select e from Session e where e.userId = :userId")
            .registerQuery(
                "findAllByUserIdIn",
                "select e from Session e where e.userId IN :userIds")
            .registerQuery(
                "findByUserIdAndDeviceId",
                "select e from Session e where e.userId = :userId and e.deviceId = :deviceId and e.closedReason is null")
            .registerQuery(
                "findOpenByDeviceId",
                "select e from Session e where e.deviceId = :deviceId and e.closedReason is null")
            .registerQuery(
                "findOpenByUserId",
                "select e from Session e where e.userId = :userId and e.closedReason is null");
    }

    @Override
    public Map<Long,Optional<Session>>
    findAllByIdIn(Set<Long> ids)
    {
        Map<Long,Optional<Session>> result =
            getUnitOfWork()
                .findManyByQuery(
                    Session.class,
                    "findAllByIdIn",
                    Map.of("sessionIds",ids))
                .stream()
                .collect(
                    Collectors.toMap(
                        Session::getPrimaryId,
                        Optional::ofNullable));

        ids
            .stream()
            .filter(id -> !result.containsKey(id))
            .forEach(id -> result.put(id,Optional.empty()));

        return result;
    }

    @Override
    public Optional<Session>
    findByAccessToken(String accessToken)
    {
        return
            getUnitOfWork()
                .findOneByQuery(
                    Session.class,
                    "findByAccessToken",
                    Map.of("accessToken",accessToken));
    }

    @Override
    public Optional<Session>
    findByRefreshTokenHash(String hash)
    {
        return
            getUnitOfWork()
                .findOneByQuery(
                    Session.class,
                    "findByRefreshTokenHash",
                    Map.of("hash",hash));
    }

    @Override
    public List<Session>
    findExpiredBefore(Instant time)
    {
        return
            getUnitOfWork()
                .findManyByQuery(
                    Session.class,
                    "findExpiredBefore",
                    Map.of("time",time))
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Session>
    findByPreviousRefreshTokenHash(String hash)
    {
        return
            getUnitOfWork()
                .findOneByQuery(
                    Session.class,
                    "findByPreviousRefreshTokenHash",
                    Map.of("hash",hash));
    }

    @Override
    public Optional<Session>
    findByHashedHandle(String hash)
    {
        return
            getUnitOfWork()
                .findOneByQuery(
                    Session.class,
                    "findByHashedHandle",
                    Map.of("hash",hash));
    }

    @Override
    public List<Session>
    findAllByUserId(Long userId)
    {
        return
            getUnitOfWork()
                .findManyByQuery(
                    Session.class,
                    "findAllByUserId",
                    Map.of("userId",userId))
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public IMultiMap<Long,Session>
    findAllByUserIdIn(Set<Long> userIds)
    {
        ListValuedMultiMap<Long,Session> result =
            new ListValuedMultiMap<>();

        getUnitOfWork()
            .findManyByQuery(
                Session.class,
                "findAllByUserIdIn",
                Map.of("userIds",userIds))
            .forEach(entity -> result.put(entity.getUserId(),entity));

        userIds
            .stream()
            .filter(userId -> !result.containsKey(userId))
            .forEach(userId -> result.putAll(userId,List.of()));

        return result;
    }

    @Override
    public Optional<Session>
    findByUserIdAndDeviceId(Long userId,String deviceId)
    {
        return
            getUnitOfWork()
                .findOneByQuery(
                    Session.class,
                    "findByUserIdAndDeviceId",
                    Map.of("userId",userId,"deviceId",deviceId));
    }

    @Override
    public List<Session>
    findOpenByDeviceId(String deviceId)
    {
        return
            getUnitOfWork()
                .findManyByQuery(
                    Session.class,
                    "findOpenByDeviceId",
                    Map.of("deviceId",deviceId))
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public List<Session>
    findOpenByUserId(Long userId)
    {
        return
            getUnitOfWork()
                .findManyByQuery(
                    Session.class,
                    "findOpenByUserId",
                    Map.of("userId",userId))
                .stream()
                .collect(Collectors.toList());
    }
}

//////////////////////////////////////////////////////////////////////////////
