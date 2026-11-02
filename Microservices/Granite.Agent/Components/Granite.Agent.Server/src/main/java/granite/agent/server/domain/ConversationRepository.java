//////////////////////////////////////////////////////////////////////////////
// ConversationRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.domain;

import strata.server.core.repository.AbstractRepository;
import strata.server.core.unitofwork.IUnitOfWork;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public
class ConversationRepository
    extends AbstractRepository<Long,Conversation>
    implements IConversationRepository
{
    public
    ConversationRepository(IUnitOfWork uow)
    {
        super(Conversation.class,"primaryId",uow);
        getUnitOfWork()
            .registerQuery(
                "findAllConversationsByUserId",
                "select e from Conversation e where e.userId = :userId order by e.primaryId desc");
    }

    @Override
    public List<Conversation>
    findAllByUserId(Long userId)
    {
        return new ArrayList<>(getUnitOfWork().findManyByQuery(Conversation.class,"findAllConversationsByUserId",Map.of("userId",userId)));
    }
}

//////////////////////////////////////////////////////////////////////////////
