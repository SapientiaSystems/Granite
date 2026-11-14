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
import java.util.function.Supplier;

public
class ClientRepository
    extends AbstractRepository<Long,Client>
    implements IClientRepository
{
    private static final String RECORD_QUERY = "recordClientTokenIssued";

    private final Supplier<IUnitOfWork> independentUnitOfWork;

    public
    ClientRepository(IUnitOfWork uow,Supplier<IUnitOfWork> independent)
    {
        super(Client.class,"primaryId",uow);
        independentUnitOfWork = independent;
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
                "select e from Client e order by e.name");
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
        try (IUnitOfWork uow = independentUnitOfWork.get())
        {
            uow.registerQuery(
                RECORD_QUERY,
                "update Client e set e.lastTokenIssuedAt = :at where e.clientId = :clientId");
            uow.begin();

            try
            {
                int updated =
                    uow.executeUpdateOrDelete(
                        RECORD_QUERY,
                        Map.of("clientId",clientId,"at",at));

                uow.commit();
                return updated;
            }
            catch (RuntimeException e)
            {
                uow.rollback();
                throw e;
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
