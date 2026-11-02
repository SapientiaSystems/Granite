//////////////////////////////////////////////////////////////////////////////
// InitializeSurveyView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.survey;

import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SurveyOutput;
import granite.frontendcontroller.service.requestreply.GetSurveyOutputRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeSurveyView
    implements IOutputAction<GetSurveyOutputRequest,SurveyOutput>
{
    private final SessionRequestContext context;

    public
    InitializeSurveyView(SessionRequestContext context)
    {
        this.context = context;
    }

    @Override
    public CompletionStage<SurveyOutput>
    apply(GetSurveyOutputRequest request)
    {
        SurveyOutput output =
            context.getCallerUserId().isEmpty()
                ? UnauthenticatedSurveyOutputs.survey()
                : new SurveyOutput();

        return
            CompletableFuture.completedFuture(
                output
                    .setUserId(context.getCallerUserId().orElse(null))
                    .setSource(request.getSource())
                    .setCommand(request.getCommand())
                    .setDestination(request.getDestination()));
    }
}

//////////////////////////////////////////////////////////////////////////////
