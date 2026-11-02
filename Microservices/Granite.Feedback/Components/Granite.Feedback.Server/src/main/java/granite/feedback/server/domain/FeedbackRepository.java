//////////////////////////////////////////////////////////////////////////////
// FeedbackRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.domain;

import granite.feedback.service.requestreply.FeedbackStatus;
import strata.server.core.repository.AbstractRepository;
import strata.server.core.unitofwork.IUnitOfWork;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public
class FeedbackRepository
    extends AbstractRepository<Long,Feedback>
    implements IFeedbackRepository
{
    public
    FeedbackRepository(IUnitOfWork uow)
    {
        super(Feedback.class,"primaryId",uow);
        getUnitOfWork()
            .registerQuery(
                "findAllFeedbackByIdIn",
                "select e from Feedback e where e.primaryId IN :feedbackIds")
            .registerQuery(
                "findAllFeedback",
                "select e from Feedback e order by e.primaryId desc")
            .registerQuery(
                "findAllFeedbackByStatusIn",
                "select e from Feedback e where e.status IN :statuses order by e.primaryId desc");
    }

    @Override
    public Map<Long,Optional<Feedback>>
    findAllByIdIn(Set<Long> ids)
    {
        Map<Long,Optional<Feedback>> result =
            getUnitOfWork()
                .findManyByQuery(
                    Feedback.class,
                    "findAllFeedbackByIdIn",
                    Map.of("feedbackIds",ids))
                .stream()
                .collect(
                    Collectors.toMap(
                        Feedback::getPrimaryId,
                        Optional::ofNullable));

        ids
            .stream()
            .filter(id -> !result.containsKey(id))
            .forEach(id -> result.put(id,Optional.empty()));

        return result;
    }

    @Override
    public List<Feedback>
    findAllFeedback()
    {
        return
            getUnitOfWork()
                .findManyByQuery(Feedback.class,"findAllFeedback",Map.of())
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public List<Feedback>
    findAllByStatusIn(Set<FeedbackStatus> statuses)
    {
        return
            getUnitOfWork()
                .findManyByQuery(Feedback.class,"findAllFeedbackByStatusIn",Map.of("statuses",statuses))
                .stream()
                .collect(Collectors.toList());
    }
}

//////////////////////////////////////////////////////////////////////////////
