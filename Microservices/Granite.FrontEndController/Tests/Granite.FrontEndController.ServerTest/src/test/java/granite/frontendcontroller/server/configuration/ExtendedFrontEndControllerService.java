//////////////////////////////////////////////////////////////////////////////
// ExtendedFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.application.GraniteFrontEndControllerContext;
import granite.frontendcontroller.server.application.GraniteFrontEndControllerService;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.SessionOutput;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class ExtendedFrontEndControllerService
    extends    GraniteFrontEndControllerService
    implements IExtendedFrontEndControllerService
{
    public
    ExtendedFrontEndControllerService(GraniteFrontEndControllerContext context)
    {
        super(context);
    }

    @Override
    public CompletionStage<GetSessionOutputReply>
    getEchoOutput(GetSessionOutputRequest request)
    {
        return
            CompletableFuture.completedFuture(
                new GetSessionOutputReply(request)
                    .setSuccess(true)
                    .setOutput(new SessionOutput().setUserName("echo")));
    }
}

//////////////////////////////////////////////////////////////////////////////
