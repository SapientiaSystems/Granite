//////////////////////////////////////////////////////////////////////////////
// UsersFrontEndControllerServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.service.requestreply.GetUsersOutputReply;
import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleUsersInputReply;
import granite.frontendcontroller.service.requestreply.HandleUsersInputRequest;
import granite.frontendcontroller.service.requestreply.IUsersFrontEndControllerService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import strata.frontendcontroller.server.platform.AbstractFrontEndControllerServiceController;

import java.util.concurrent.CompletionStage;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("${granite.frontendcontroller.service-path}")
public
class UsersFrontEndControllerServiceController
    extends AbstractFrontEndControllerServiceController<IUsersFrontEndControllerService>
{
    @Inject
    public
    UsersFrontEndControllerServiceController(
        @Named("Users")
        IUsersFrontEndControllerService service)
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
}

//////////////////////////////////////////////////////////////////////////////
