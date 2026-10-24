//////////////////////////////////////////////////////////////////////////////
// ISessionRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import strata.foundation.core.collection.IMultiMap;
import strata.server.core.repository.IRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
interface ISessionRepository
    extends IRepository<Long,Session>
{
    Map<Long,Optional<Session>>
    findAllByIdIn(Set<Long> ids);

    Optional<Session>
    findByAccessToken(String accessToken);

    Optional<Session>
    findByRefreshTokenHash(String hash);

    List<Session>
    findExpiredBefore(Instant time);

    Optional<Session>
    findByPreviousRefreshTokenHash(String hash);

    Optional<Session>
    findByHashedHandle(String hash);

    List<Session>
    findAllByUserId(Long userId);

    IMultiMap<Long,Session>
    findAllByUserIdIn(Set<Long> userIds);

    Optional<Session>
    findByUserIdAndDeviceId(Long userId,String deviceId);
}

//////////////////////////////////////////////////////////////////////////////
