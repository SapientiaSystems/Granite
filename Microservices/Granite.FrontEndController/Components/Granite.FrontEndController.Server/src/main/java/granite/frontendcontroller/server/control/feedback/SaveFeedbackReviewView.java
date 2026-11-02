//////////////////////////////////////////////////////////////////////////////
// SaveFeedbackReviewView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.feedback;

import granite.feedback.service.requestreply.UpdateFeedbackData;
import granite.feedback.service.requestreply.UpdateFeedbacksReply;
import granite.feedback.service.requestreply.UpdateFeedbacksRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.FeedbackReviewInput;
import granite.frontendcontroller.service.requestreply.FeedbackReviewOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SaveFeedbackReviewView
    implements IInputAction<FeedbackReviewInput,FeedbackReviewOutput>
{
    private static final String NOT_SAVED = "not saved";

    private final GraniteHelpServiceClients clients;
    private final FeedbackReviewLoader      loader;
    private final SessionRequestContext     context;

    public
    SaveFeedbackReviewView(
        GraniteHelpServiceClients clients,
        FeedbackReviewLoader      loader,
        SessionRequestContext     context)
    {
        this.clients = clients;
        this.loader  = loader;
        this.context = context;
    }

    @Override
    public CompletionStage<FeedbackReviewOutput>
    apply(FeedbackReviewInput input)
    {
        if (context.getCallerUserId().isEmpty())
            return CompletableFuture.completedFuture(describe(input,UnauthenticatedFeedbackOutputs.review()));

        List<String>             failures = new ArrayList<>();
        List<UpdateFeedbackData> changes  = new ArrayList<>();

        for (UpdateFeedbackData change : input.getChanges() == null ? List.<UpdateFeedbackData>of() : input.getChanges())
            if (change != null && change.getFeedbackId() != null)
                changes.add(change);

        return
            save(changes,failures)
                .thenCompose(v -> loader.load(failures))
                .thenApply(output -> describe(input,output));
    }

    protected CompletionStage<Void>
    save(List<UpdateFeedbackData> changes,List<String> failures)
    {
        if (changes.isEmpty())
            return CompletableFuture.completedFuture(null);

        return
            clients
                .getFeedbackCommandClient()
                .updateFeedbacks(new UpdateFeedbacksRequest().setFeedback(changes))
                .thenAccept(reply -> collectFailures(reply,failures));
    }

    protected void
    collectFailures(UpdateFeedbacksReply reply,List<String> failures)
    {
        if (reply == null)
        {
            failures.add(NOT_SAVED);
            return;
        }

        Set<Long> failed = reply.getFailedFeedbackIds();

        if (failed != null && !failed.isEmpty())
        {
            for (Long id : failed)
                failures.add(id + ": " + NOT_SAVED);
        }
        else if (!reply.isSuccess())
            failures.add(reply.getFailureMessage() == null || reply.getFailureMessage().isBlank() ? NOT_SAVED : reply.getFailureMessage());
    }

    private FeedbackReviewOutput
    describe(FeedbackReviewInput input,FeedbackReviewOutput output)
    {
        return
            output
                .setUserId(context.getCallerUserId().orElse(null))
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }
}

//////////////////////////////////////////////////////////////////////////////
