//////////////////////////////////////////////////////////////////////////////
// InitializeSurveyReviewView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.survey;

import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SurveyReviewOutput;
import granite.frontendcontroller.service.requestreply.GetSurveyReviewOutputRequest;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeSurveyReviewView
    implements IOutputAction<GetSurveyReviewOutputRequest,SurveyReviewOutput>
{
    private final SurveyReviewLoader  loader;
    private final SessionRequestContext context;

    public
    InitializeSurveyReviewView(SurveyReviewLoader loader,SessionRequestContext context)
    {
        this.loader  = loader;
        this.context = context;
    }

    @Override
    public CompletionStage<SurveyReviewOutput>
    apply(GetSurveyReviewOutputRequest request)
    {
        CompletionStage<SurveyReviewOutput> output =
            context.getCallerUserId().isEmpty()
                ? CompletableFuture.completedFuture(UnauthenticatedSurveyOutputs.review())
                : loader.load(new ArrayList<>());

        return
            output.thenApply(
                result ->
                    result
                        .setUserId(context.getCallerUserId().orElse(null))
                        .setSource(request.getSource())
                        .setCommand(request.getCommand())
                        .setDestination(request.getDestination()));
    }
}

//////////////////////////////////////////////////////////////////////////////
