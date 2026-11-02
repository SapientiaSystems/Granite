//////////////////////////////////////////////////////////////////////////////
// FeedbackQueryServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.platform;

import granite.feedback.service.requestreply.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletionStage;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/feedback-query-service")
@EnableTransactionManagement
public
class FeedbackQueryServiceController
{
    private final IFeedbackQueryService implementation;
    private final Logger                logger;

    @Inject
    public
    FeedbackQueryServiceController(IFeedbackQueryService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(FeedbackQueryServiceController.class);
    }

    @PostMapping(value = "/find-feedback-by-ids",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindFeedbackByIdsReply>
    findFeedbackByIds(@RequestBody FindFeedbackByIdsRequest request)
    {
        logger.info("delegating findFeedbackByIds to implementation service");
        return implementation.findFeedbackByIds(request);
    }

    @PostMapping(value = "/find-all-feedback",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindAllFeedbackReply>
    findAllFeedback(@RequestBody FindAllFeedbackRequest request)
    {
        logger.info("delegating findAllFeedback to implementation service");
        return implementation.findAllFeedback(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
