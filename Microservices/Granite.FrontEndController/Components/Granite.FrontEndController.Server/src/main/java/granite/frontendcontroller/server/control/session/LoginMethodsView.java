//////////////////////////////////////////////////////////////////////////////
// LoginMethodsView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.SessionOutput;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class LoginMethodsView
    implements IOutputAction<GetSessionOutputRequest,SessionOutput>
{
    private final SessionLoginSettings settings;

    public
    LoginMethodsView(SessionLoginSettings settings)
    {
        this.settings = settings;
    }

    @Override
    public CompletionStage<SessionOutput>
    apply(GetSessionOutputRequest request)
    {
        return
            CompletableFuture.completedFuture(
                new SessionOutput()
                    .setPasswordEnabled(settings.isPasswordEnabled())
                    .setOktaEnabled(settings.isOktaEnabled())
                    .setUserId(request.getUserId())
                    .setSource(request.getSource())
                    .setCommand(request.getCommand())
                    .setDestination(request.getDestination()));
    }
}

//////////////////////////////////////////////////////////////////////////////
