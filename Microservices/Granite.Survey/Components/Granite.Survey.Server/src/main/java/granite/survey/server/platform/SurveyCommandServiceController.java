//////////////////////////////////////////////////////////////////////////////
// SurveyCommandServiceController.java
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
@RequestMapping("/survey-command-service")
@EnableTransactionManagement
public
class SurveyCommandServiceController
{
    private final ISurveyCommandService implementation;
    private final Logger                 logger;

    @Inject
    public
    SurveyCommandServiceController(ISurveyCommandService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(SurveyCommandServiceController.class);
    }

    @PostMapping(value = "/submit-survey-response",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<SubmitSurveyResponseReply>
    submitSurveyResponse(@RequestBody SubmitSurveyResponseRequest request)
    {
        logger.info("delegating submitSurveyResponse to implementation service");
        return implementation.submitSurveyResponse(request);
    }

    @PostMapping(value = "/update-survey-responses",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<UpdateSurveyResponsesReply>
    updateSurveyResponses(@RequestBody UpdateSurveyResponsesRequest request)
    {
        logger.info("delegating updateSurveyResponses to implementation service");
        return implementation.updateSurveyResponses(request);
    }

    @PostMapping(value = "/mark-survey-responses-sent",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<MarkSurveyResponsesSentReply>
    markSurveyResponsesSent(@RequestBody MarkSurveyResponsesSentRequest request)
    {
        logger.info("delegating markSurveyResponsesSent to implementation service");
        return implementation.markSurveyResponsesSent(request);
    }

    @PostMapping(value = "/mark-survey-responses-received",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<MarkSurveyResponsesReceivedReply>
    markSurveyResponsesReceived(@RequestBody MarkSurveyResponsesReceivedRequest request)
    {
        logger.info("delegating markSurveyResponsesReceived to implementation service");
        return implementation.markSurveyResponsesReceived(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
