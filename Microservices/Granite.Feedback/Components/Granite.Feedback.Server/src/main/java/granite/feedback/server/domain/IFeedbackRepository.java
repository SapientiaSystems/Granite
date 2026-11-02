//////////////////////////////////////////////////////////////////////////////
// IFeedbackRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.domain;

import granite.feedback.service.requestreply.FeedbackStatus;
import strata.server.core.repository.IRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
interface IFeedbackRepository
    extends IRepository<Long,Feedback>
{
    Map<Long,Optional<Feedback>>
    findAllByIdIn(Set<Long> ids);

    List<Feedback>
    findAllFeedback();

    List<Feedback>
    findAllByStatusIn(Set<FeedbackStatus> statuses);
}

//////////////////////////////////////////////////////////////////////////////
