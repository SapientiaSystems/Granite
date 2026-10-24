//////////////////////////////////////////////////////////////////////////////
// IOktaLoginTransactionRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import strata.server.core.repository.IRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public
interface IOktaLoginTransactionRepository
    extends IRepository<Long,OktaLoginTransaction>
{
    Optional<OktaLoginTransaction>
    findByHashedTransactionId(String hash);

    List<OktaLoginTransaction>
    findExpiredBefore(Instant time);
}

//////////////////////////////////////////////////////////////////////////////
