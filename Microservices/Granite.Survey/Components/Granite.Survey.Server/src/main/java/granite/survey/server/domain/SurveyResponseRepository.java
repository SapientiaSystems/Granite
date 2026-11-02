//////////////////////////////////////////////////////////////////////////////
// SurveyResponseRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.domain;

import granite.survey.service.requestreply.SurveyResponseStatus;
import strata.server.core.repository.AbstractRepository;
import strata.server.core.unitofwork.IUnitOfWork;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public
class SurveyResponseRepository
    extends AbstractRepository<Long,SurveyResponse>
    implements ISurveyResponseRepository
{
    public
    SurveyResponseRepository(IUnitOfWork uow)
    {
        super(SurveyResponse.class,"primaryId",uow);
        getUnitOfWork()
            .registerQuery(
                "findAllSurveyResponsesByIdIn",
                "select e from SurveyResponse e where e.primaryId IN :responseIds")
            .registerQuery(
                "findAllSurveyResponses",
                "select e from SurveyResponse e order by e.primaryId desc")
            .registerQuery(
                "findAllSurveyResponsesByStatusIn",
                "select e from SurveyResponse e where e.status IN :statuses order by e.primaryId desc");
    }

    @Override
    public Map<Long,Optional<SurveyResponse>>
    findAllByIdIn(Set<Long> ids)
    {
        Map<Long,Optional<SurveyResponse>> result =
            getUnitOfWork()
                .findManyByQuery(
                    SurveyResponse.class,
                    "findAllSurveyResponsesByIdIn",
                    Map.of("responseIds",ids))
                .stream()
                .collect(
                    Collectors.toMap(
                        SurveyResponse::getPrimaryId,
                        Optional::ofNullable));

        ids
            .stream()
            .filter(id -> !result.containsKey(id))
            .forEach(id -> result.put(id,Optional.empty()));

        return result;
    }

    @Override
    public List<SurveyResponse>
    findAllResponses()
    {
        return
            getUnitOfWork()
                .findManyByQuery(SurveyResponse.class,"findAllSurveyResponses",Map.of())
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public List<SurveyResponse>
    findAllByStatusIn(Set<SurveyResponseStatus> statuses)
    {
        return
            getUnitOfWork()
                .findManyByQuery(SurveyResponse.class,"findAllSurveyResponsesByStatusIn",Map.of("statuses",statuses))
                .stream()
                .collect(Collectors.toList());
    }
}

//////////////////////////////////////////////////////////////////////////////
