//////////////////////////////////////////////////////////////////////////////
// IClientRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.domain;

import strata.server.core.repository.IRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public
interface IClientRepository
    extends IRepository<Long,Client>
{
    Optional<Client>
    findByClientId(String clientId);

    Optional<Client>
    findByName(String name);

    List<Client>
    findAllByClientIdIn(Set<String> clientIds);

    List<Client>
    findAllClients();

    List<Client>
    findAllByOwner(Long userId);

    int
    recordTokenIssued(String clientId,Instant at);
}

//////////////////////////////////////////////////////////////////////////////
