//////////////////////////////////////////////////////////////////////////////
// LoginMethodsView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.service.requestreply.LoginMethodsOutput;
import strata.frontendcontroller.server.application.IGetOutputAction;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class LoginMethodsView
    implements IGetOutputAction
{
    private final SessionLoginSettings settings;

    public
    LoginMethodsView(SessionLoginSettings settings)
    {
        this.settings = settings;
    }

    @Override
    public CompletionStage<FrontEndOutput>
    apply(GetOutputRequest request)
    {
        return
            CompletableFuture.completedFuture(
                new LoginMethodsOutput()
                    .setPasswordEnabled(settings.isPasswordEnabled())
                    .setOktaEnabled(settings.isOktaEnabled())
                    .setUserId(request.getUserId())
                    .setSource(request.getSource())
                    .setCommand(request.getCommand())
                    .setDestination(request.getDestination()));
    }
}

//////////////////////////////////////////////////////////////////////////////
