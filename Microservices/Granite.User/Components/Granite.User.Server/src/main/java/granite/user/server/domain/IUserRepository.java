//////////////////////////////////////////////////////////////////////////////
// IUserRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import strata.foundation.core.collection.IMultiMap;
import strata.server.core.repository.IRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
interface IUserRepository
    extends IRepository<Long,User>
{
    Map<Long,Optional<User>>
    findAllByIdIn(Set<Long> ids);

    Optional<User>
    findByUserName(String userName);

    IMultiMap<String,User>
    findAllByUserNameIn(Set<String> userNames);

    List<User>
    findAllByExternalIdentity(String externalIdentity);
}

//////////////////////////////////////////////////////////////////////////////