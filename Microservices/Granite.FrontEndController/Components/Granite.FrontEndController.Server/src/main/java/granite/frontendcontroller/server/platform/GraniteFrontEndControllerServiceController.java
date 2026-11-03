//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.service.requestreply.HandleFeedbackInputRequest;
import granite.frontendcontroller.service.requestreply.HandleAgentInputRequest;
import granite.frontendcontroller.service.requestreply.HandleAgentInputReply;
import granite.frontendcontroller.service.requestreply.GetAgentOutputRequest;
import granite.frontendcontroller.service.requestreply.GetAgentOutputReply;
import granite.frontendcontroller.service.requestreply.HandleContentInputRequest;
import granite.frontendcontroller.service.requestreply.HandleContentInputReply;
import granite.frontendcontroller.service.requestreply.GetContentOutputRequest;
import granite.frontendcontroller.service.requestreply.GetContentOutputReply;
import granite.frontendcontroller.service.requestreply.HandleSurveyInputRequest;
import granite.frontendcontroller.service.requestreply.HandleFeedbackInputReply;
import granite.frontendcontroller.service.requestreply.HandleSurveyInputReply;
import granite.frontendcontroller.service.requestreply.GetFeedbackOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSurveyOutputRequest;
import granite.frontendcontroller.service.requestreply.GetFeedbackOutputReply;
import granite.frontendcontroller.service.requestreply.GetSurveyOutputReply;
import granite.frontendcontroller.service.requestreply.HandleFeedbackReviewInputRequest;
import granite.frontendcontroller.service.requestreply.HandleSurveyReviewInputRequest;
import granite.frontendcontroller.service.requestreply.HandleFeedbackReviewInputReply;
import granite.frontendcontroller.service.requestreply.HandleSurveyReviewInputReply;
import granite.frontendcontroller.service.requestreply.GetFeedbackReviewOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSurveyReviewOutputRequest;
import granite.frontendcontroller.service.requestreply.GetFeedbackReviewOutputReply;
import granite.frontendcontroller.service.requestreply.GetSurveyReviewOutputReply;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputReply;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.GetHomeOutputReply;
import granite.frontendcontroller.service.requestreply.GetHomeOutputRequest;
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
        value = "/handle-agent-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandleAgentInputReply>
    handleAgentInput(@RequestBody HandleAgentInputRequest request)
    {
        getLogger().info("delegating handleAgentInput to implementation service");
        return getService().handleAgentInput(request);
    }

    @PostMapping(
        value = "/get-agent-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetAgentOutputReply>
    getAgentOutput(@RequestBody GetAgentOutputRequest request)
    {
        getLogger().info("delegating getAgentOutput to implementation service");
        return getService().getAgentOutput(request);
    }

    @PostMapping(
        value = "/handle-content-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandleContentInputReply>
    handleContentInput(@RequestBody HandleContentInputRequest request)
    {
        getLogger().info("delegating handleContentInput to implementation service");
        return getService().handleContentInput(request);
    }

    @PostMapping(
        value = "/get-content-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetContentOutputReply>
    getContentOutput(@RequestBody GetContentOutputRequest request)
    {
        getLogger().info("delegating getContentOutput to implementation service");
        return getService().getContentOutput(request);
    }

    @PostMapping(
        value = "/handle-survey-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandleSurveyInputReply>
    handleSurveyInput(@RequestBody HandleSurveyInputRequest request)
    {
        getLogger().info("delegating handleSurveyInput to implementation service");
        return getService().handleSurveyInput(request);
    }

    @PostMapping(
        value = "/get-survey-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetSurveyOutputReply>
    getSurveyOutput(@RequestBody GetSurveyOutputRequest request)
    {
        getLogger().info("delegating getSurveyOutput to implementation service");
        return getService().getSurveyOutput(request);
    }

    @PostMapping(
        value = "/handle-survey-review-input",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<HandleSurveyReviewInputReply>
    handleSurveyReviewInput(@RequestBody HandleSurveyReviewInputRequest request)
    {
        getLogger().info("delegating handleSurveyReviewInput to implementation service");
        return getService().handleSurveyReviewInput(request);
    }

    @PostMapping(
        value = "/get-survey-review-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetSurveyReviewOutputReply>
    getSurveyReviewOutput(@RequestBody GetSurveyReviewOutputRequest request)
    {
        getLogger().info("delegating getSurveyReviewOutput to implementation service");
        return getService().getSurveyReviewOutput(request);
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
        value = "/get-home-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetHomeOutputReply>
    getHomeOutput(@RequestBody GetHomeOutputRequest request)
    {
        getLogger().info("delegating getHomeOutput to implementation service");
        return getService().getHomeOutput(request);
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
