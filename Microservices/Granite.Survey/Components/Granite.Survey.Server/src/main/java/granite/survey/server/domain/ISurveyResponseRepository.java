//////////////////////////////////////////////////////////////////////////////
// ISurveyResponseRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.domain;

import granite.survey.service.requestreply.SurveyResponseStatus;
import strata.server.core.repository.IRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
interface ISurveyResponseRepository
    extends IRepository<Long,SurveyResponse>
{
    Map<Long,Optional<SurveyResponse>>
    findAllByIdIn(Set<Long> ids);

    List<SurveyResponse>
    findAllResponses();

    List<SurveyResponse>
    findAllByStatusIn(Set<SurveyResponseStatus> statuses);
}

//////////////////////////////////////////////////////////////////////////////
