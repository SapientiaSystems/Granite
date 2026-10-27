//////////////////////////////////////////////////////////////////////////////
// ExtendedFrontEndControllerServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.platform.GraniteFrontEndControllerServiceController;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.CompletionStage;

public
class ExtendedFrontEndControllerServiceController
    extends GraniteFrontEndControllerServiceController<IExtendedFrontEndControllerService>
{
    public
    ExtendedFrontEndControllerServiceController(IExtendedFrontEndControllerService service)
    {
        super(service);
    }

    @PostMapping(
        value = "/get-echo-output",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<GetSessionOutputReply>
    getEchoOutput(@RequestBody GetSessionOutputRequest request)
    {
        return getService().getEchoOutput(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
