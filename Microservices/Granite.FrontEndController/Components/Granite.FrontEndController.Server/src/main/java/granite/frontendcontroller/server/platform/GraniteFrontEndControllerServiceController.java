//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.service.requestreply.HandleFeedbackInputRequest;
import granite.frontendcontroller.service.requestreply.HandleFeedbackInputReply;
import granite.frontendcontroller.service.requestreply.GetFeedbackOutputRequest;
import granite.frontendcontroller.service.requestreply.GetFeedbackOutputReply;
import granite.frontendcontroller.service.requestreply.HandleFeedbackReviewInputRequest;
import granite.frontendcontroller.service.requestreply.HandleFeedbackReviewInputReply;
import granite.frontendcontroller.service.requestreply.GetFeedbackReviewOutputRequest;
import granite.frontendcontroller.service.requestreply.GetFeedbackReviewOutputReply;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputReply;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSessionsOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionsOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleSessionsInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionsInputRequest;
import granite.frontendcontroller.service.requestreply.GetUsersOutputReply;
import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;
import granite.frontendcontroller.service.requestreply.HandlePreferencesInputReply;
import granite.frontendcontroller.service.requestreply.HandlePreferencesInputRequest;
import granite.frontendcontroller.service.requestreply.HandleSessionInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionInputRequest;
import granite.frontendcontroller.service.requestreply.HandleUsersInputReply;
import granite.frontendcontroller.service.requestreply.HandleUsersInputRequest;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import strata.frontendcontroller.server.platform.AbstractFrontEndControllerServiceController;

import java.util.concurrent.CompletionStage;

public
class GraniteFrontEndControllerServiceController<S extends IGraniteFrontEndControllerService>
    extends AbstractFrontEndControllerServiceController<S>
{
    public
    GraniteFrontEndControllerServiceController(S service)
    {
        super(service);
    }

    @PostMapping(
        value = "/handle-users-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandleUsersInputReply>
    handleUsersInput(@RequestBody HandleUsersInputRequest request)
    {
        getLogger().info("delegating handleUsersInput to implementation service");
        return getService().handleUsersInput(request);
    }

    @PostMapping(
        value = "/get-users-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetUsersOutputReply>
    getUsersOutput(@RequestBody GetUsersOutputRequest request)
    {
        getLogger().info("delegating getUsersOutput to implementation service");
        return getService().getUsersOutput(request);
    }

    @PostMapping(
        value = "/handle-feedback-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandleFeedbackInputReply>
    handleFeedbackInput(@RequestBody HandleFeedbackInputRequest request)
    {
        getLogger().info("delegating handleFeedbackInput to implementation service");
        return getService().handleFeedbackInput(request);
    }

    @PostMapping(
        value = "/get-feedback-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetFeedbackOutputReply>
    getFeedbackOutput(@RequestBody GetFeedbackOutputRequest request)
    {
        getLogger().info("delegating getFeedbackOutput to implementation service");
        return getService().getFeedbackOutput(request);
    }

    @PostMapping(
        value = "/handle-feedback-review-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandleFeedbackReviewInputReply>
    handleFeedbackReviewInput(@RequestBody HandleFeedbackReviewInputRequest request)
    {
        getLogger().info("delegating handleFeedbackReviewInput to implementation service");
        return getService().handleFeedbackReviewInput(request);
    }

    @PostMapping(
        value = "/get-feedback-review-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetFeedbackReviewOutputReply>
    getFeedbackReviewOutput(@RequestBody GetFeedbackReviewOutputRequest request)
    {
        getLogger().info("delegating getFeedbackReviewOutput to implementation service");
        return getService().getFeedbackReviewOutput(request);
    }

    @PostMapping(
        value = "/handle-sessions-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandleSessionsInputReply>
    handleSessionsInput(@RequestBody HandleSessionsInputRequest request)
    {
        getLogger().info("delegating handleSessionsInput to implementation service");
        return getService().handleSessionsInput(request);
    }

    @PostMapping(
        value = "/get-sessions-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetSessionsOutputReply>
    getSessionsOutput(@RequestBody GetSessionsOutputRequest request)
    {
        getLogger().info("delegating getSessionsOutput to implementation service");
        return getService().getSessionsOutput(request);
    }

    @PostMapping(
        value = "/handle-session-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandleSessionInputReply>
    handleSessionInput(@RequestBody HandleSessionInputRequest request)
    {
        getLogger().info("delegating handleSessionInput to implementation service");
        return getService().handleSessionInput(request);
    }

    @PostMapping(
        value = "/get-session-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetSessionOutputReply>
    getSessionOutput(@RequestBody GetSessionOutputRequest request)
    {
        getLogger().info("delegating getSessionOutput to implementation service");
        return getService().getSessionOutput(request);
    }

    @PostMapping(
        value = "/handle-preferences-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandlePreferencesInputReply>
    handlePreferencesInput(@RequestBody HandlePreferencesInputRequest request)
    {
        getLogger().info("delegating handlePreferencesInput to implementation service");
        return getService().handlePreferencesInput(request);
    }

    @PostMapping(
        value = "/get-preferences-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetPreferencesOutputReply>
    getPreferencesOutput(@RequestBody GetPreferencesOutputRequest request)
    {
        getLogger().info("delegating getPreferencesOutput to implementation service");
        return getService().getPreferencesOutput(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
