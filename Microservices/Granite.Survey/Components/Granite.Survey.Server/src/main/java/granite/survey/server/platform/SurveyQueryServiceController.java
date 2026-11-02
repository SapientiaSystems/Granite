//////////////////////////////////////////////////////////////////////////////
// SurveyQueryServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.platform;

import granite.survey.service.requestreply.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletionStage;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/survey-query-service")
@EnableTransactionManagement
public
class SurveyQueryServiceController
{
    private final ISurveyQueryService implementation;
    private final Logger               logger;

    @Inject
    public
    SurveyQueryServiceController(ISurveyQueryService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(SurveyQueryServiceController.class);
    }

    @PostMapping(value = "/find-survey-responses-by-ids",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindSurveyResponsesByIdsReply>
    findSurveyResponsesByIds(@RequestBody FindSurveyResponsesByIdsRequest request)
    {
        logger.info("delegating findSurveyResponsesByIds to implementation service");
        return implementation.findSurveyResponsesByIds(request);
    }

    @PostMapping(value = "/find-all-survey-responses",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindAllSurveyResponsesReply>
    findAllSurveyResponses(@RequestBody FindAllSurveyResponsesRequest request)
    {
        logger.info("delegating findAllSurveyResponses to implementation service");
        return implementation.findAllSurveyResponses(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
