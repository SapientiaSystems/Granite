//////////////////////////////////////////////////////////////////////////////
// InitializeFeedbackView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.feedback;

import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.FeedbackOutput;
import granite.frontendcontroller.service.requestreply.GetFeedbackOutputRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeFeedbackView
    implements IOutputAction<GetFeedbackOutputRequest,FeedbackOutput>
{
    private final SessionRequestContext context;

    public
    InitializeFeedbackView(SessionRequestContext context)
    {
        this.context = context;
    }

    @Override
    public CompletionStage<FeedbackOutput>
    apply(GetFeedbackOutputRequest request)
    {
        FeedbackOutput output =
            context.getCallerUserId().isEmpty()
                ? UnauthenticatedFeedbackOutputs.feedback()
                : new FeedbackOutput();

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
