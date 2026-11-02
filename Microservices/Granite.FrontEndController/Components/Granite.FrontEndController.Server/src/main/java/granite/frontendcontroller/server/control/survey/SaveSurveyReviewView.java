//////////////////////////////////////////////////////////////////////////////
// SaveSurveyReviewView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.survey;

import granite.survey.service.requestreply.UpdateSurveyResponseData;
import granite.survey.service.requestreply.UpdateSurveyResponsesReply;
import granite.survey.service.requestreply.UpdateSurveyResponsesRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SurveyReviewInput;
import granite.frontendcontroller.service.requestreply.SurveyReviewOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SaveSurveyReviewView
    implements IInputAction<SurveyReviewInput,SurveyReviewOutput>
{
    private static final String NOT_SAVED = "not saved";

    private final GraniteHelpServiceClients clients;
    private final SurveyReviewLoader        loader;
    private final SessionRequestContext     context;

    public
    SaveSurveyReviewView(
        GraniteHelpServiceClients clients,
        SurveyReviewLoader        loader,
        SessionRequestContext     context)
    {
        this.clients = clients;
        this.loader  = loader;
        this.context = context;
    }

    @Override
    public CompletionStage<SurveyReviewOutput>
    apply(SurveyReviewInput input)
    {
        if (context.getCallerUserId().isEmpty())
            return CompletableFuture.completedFuture(describe(input,UnauthenticatedSurveyOutputs.review()));

        List<String>                   failures = new ArrayList<>();
        List<UpdateSurveyResponseData> changes  = new ArrayList<>();

        for (UpdateSurveyResponseData change : input.getChanges() == null ? List.<UpdateSurveyResponseData>of() : input.getChanges())
            if (change != null && change.getResponseId() != null)
                changes.add(change);

        return
            save(changes,failures)
                .thenCompose(v -> loader.load(failures))
                .thenApply(output -> describe(input,output));
    }

    protected CompletionStage<Void>
    save(List<UpdateSurveyResponseData> changes,List<String> failures)
    {
        if (changes.isEmpty())
            return CompletableFuture.completedFuture(null);

        return
            clients
                .getSurveyCommandClient()
                .updateSurveyResponses(new UpdateSurveyResponsesRequest().setResponses(changes))
                .thenAccept(reply -> collectFailures(reply,failures));
    }

    protected void
    collectFailures(UpdateSurveyResponsesReply reply,List<String> failures)
    {
        if (reply == null)
        {
            failures.add(NOT_SAVED);
            return;
        }

        Set<Long> failed = reply.getFailedResponseIds();

        if (failed != null && !failed.isEmpty())
        {
            for (Long id : failed)
                failures.add(id + ": " + NOT_SAVED);
        }
        else if (!reply.isSuccess())
            failures.add(reply.getFailureMessage() == null || reply.getFailureMessage().isBlank() ? NOT_SAVED : reply.getFailureMessage());
    }

    private SurveyReviewOutput
    describe(SurveyReviewInput input,SurveyReviewOutput output)
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
