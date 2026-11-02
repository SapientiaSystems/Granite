//////////////////////////////////////////////////////////////////////////////
// FeedbackCommandServiceController.java
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
@RequestMapping("/feedback-command-service")
@EnableTransactionManagement
public
class FeedbackCommandServiceController
{
    private final IFeedbackCommandService implementation;
    private final Logger                  logger;

    @Inject
    public
    FeedbackCommandServiceController(IFeedbackCommandService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(FeedbackCommandServiceController.class);
    }

    @PostMapping(value = "/create-feedback",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<CreateFeedbackReply>
    createFeedback(@RequestBody CreateFeedbackRequest request)
    {
        logger.info("delegating createFeedback to implementation service");
        return implementation.createFeedback(request);
    }

    @PostMapping(value = "/update-feedbacks",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<UpdateFeedbacksReply>
    updateFeedbacks(@RequestBody UpdateFeedbacksRequest request)
    {
        logger.info("delegating updateFeedbacks to implementation service");
        return implementation.updateFeedbacks(request);
    }

    @PostMapping(value = "/mark-feedback-sent",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<MarkFeedbackSentReply>
    markFeedbackSent(@RequestBody MarkFeedbackSentRequest request)
    {
        logger.info("delegating markFeedbackSent to implementation service");
        return implementation.markFeedbackSent(request);
    }

    @PostMapping(value = "/mark-feedback-received",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<MarkFeedbackReceivedReply>
    markFeedbackReceived(@RequestBody MarkFeedbackReceivedRequest request)
    {
        logger.info("delegating markFeedbackReceived to implementation service");
        return implementation.markFeedbackReceived(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
