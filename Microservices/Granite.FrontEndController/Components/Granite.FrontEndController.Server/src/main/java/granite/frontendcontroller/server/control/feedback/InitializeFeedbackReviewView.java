//////////////////////////////////////////////////////////////////////////////
// InitializeFeedbackReviewView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.feedback;

import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.FeedbackReviewOutput;
import granite.frontendcontroller.service.requestreply.GetFeedbackReviewOutputRequest;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeFeedbackReviewView
    implements IOutputAction<GetFeedbackReviewOutputRequest,FeedbackReviewOutput>
{
    private final FeedbackReviewLoader  loader;
    private final SessionRequestContext context;

    public
    InitializeFeedbackReviewView(FeedbackReviewLoader loader,SessionRequestContext context)
    {
        this.loader  = loader;
        this.context = context;
    }

    @Override
    public CompletionStage<FeedbackReviewOutput>
    apply(GetFeedbackReviewOutputRequest request)
    {
        CompletionStage<FeedbackReviewOutput> output =
            context.getCallerUserId().isEmpty()
                ? CompletableFuture.completedFuture(UnauthenticatedFeedbackOutputs.review())
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
