//////////////////////////////////////////////////////////////////////////////
// OktaLoginTransactionRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import strata.server.core.repository.AbstractRepository;
import strata.server.core.unitofwork.IUnitOfWork;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public
class OktaLoginTransactionRepository
    extends AbstractRepository<Long,OktaLoginTransaction>
    implements IOktaLoginTransactionRepository
{
    public
    OktaLoginTransactionRepository(IUnitOfWork uow)
    {
        super(OktaLoginTransaction.class,"primaryId",uow);
        getUnitOfWork()
            .registerQuery(
                "findOktaLoginTransactionByHash",
                "select e from OktaLoginTransaction e where e.hashedTransactionId = :hash")
            .registerQuery(
                "findOktaLoginTransactionsExpiredBefore",
                "select e from OktaLoginTransaction e where e.expires < :time order by e.expires");
    }

    @Override
    public Optional<OktaLoginTransaction>
    findByHashedTransactionId(String hash)
    {
        return
            getUnitOfWork()
                .findOneByQuery(
                    OktaLoginTransaction.class,
                    "findOktaLoginTransactionByHash",
                    Map.of("hash",hash));
    }

    @Override
    public List<OktaLoginTransaction>
    findExpiredBefore(Instant time)
    {
        return
            getUnitOfWork()
                .findManyByQuery(
                    OktaLoginTransaction.class,
                    "findOktaLoginTransactionsExpiredBefore",
                    Map.of("time",time))
                .stream()
                .collect(Collectors.toList());
    }
}

//////////////////////////////////////////////////////////////////////////////
