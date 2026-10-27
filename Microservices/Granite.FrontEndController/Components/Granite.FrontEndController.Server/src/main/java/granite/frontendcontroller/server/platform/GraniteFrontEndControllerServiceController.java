//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.service.requestreply.GetPreferencesOutputReply;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
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
