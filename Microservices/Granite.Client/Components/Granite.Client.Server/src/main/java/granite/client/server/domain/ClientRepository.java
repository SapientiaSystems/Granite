//////////////////////////////////////////////////////////////////////////////
// ClientRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.domain;

import strata.server.core.repository.AbstractRepository;
import strata.server.core.unitofwork.IUnitOfWork;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
class ClientRepository
    extends AbstractRepository<Long,Client>
    implements IClientRepository
{
    public
    ClientRepository(IUnitOfWork uow)
    {
        super(Client.class,"primaryId",uow);
        getUnitOfWork()
            .registerQuery(
                "findClientByClientId",
                "select e from Client e where e.clientId = :clientId")
            .registerQuery(
                "findClientByName",
                "select e from Client e where e.name = :name")
            .registerQuery(
                "findAllClientsByClientIdIn",
                "select e from Client e where e.clientId IN :clientIds order by e.name")
            .registerQuery(
                "findAllClients",
                "select e from Client e order by e.name")
            .registerQuery(
                "recordClientTokenIssued",
                "update Client e set e.lastTokenIssuedAt = :at where e.clientId = :clientId");
    }

    @Override
    public Optional<Client>
    findByClientId(String clientId)
    {
        return
            getUnitOfWork()
                .findOneByQuery(Client.class,"findClientByClientId",Map.of("clientId",clientId));
    }

    @Override
    public Optional<Client>
    findByName(String name)
    {
        return
            getUnitOfWork()
                .findOneByQuery(Client.class,"findClientByName",Map.of("name",name));
    }

    @Override
    public List<Client>
    findAllByClientIdIn(Set<String> clientIds)
    {
        if (clientIds == null || clientIds.isEmpty())
            return List.of();

        return
            getUnitOfWork()
                .findManyByQuery(Client.class,"findAllClientsByClientIdIn",Map.of("clientIds",clientIds));
    }

    @Override
    public List<Client>
    findAllClients()
    {
        return getUnitOfWork().findManyByQuery(Client.class,"findAllClients",Map.of());
    }

    @Override
    public List<Client>
    findAllByOwner(Long userId)
    {
        return findAllClients().stream().filter(c -> c.isOwnedBy(userId)).toList();
    }

    @Override
    public int
    recordTokenIssued(String clientId,Instant at)
    {
        return
            getUnitOfWork()
                .executeUpdateOrDelete(
                    "recordClientTokenIssued",
                    Map.of("clientId",clientId,"at",at));
    }
}

//////////////////////////////////////////////////////////////////////////////
