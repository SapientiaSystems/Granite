//////////////////////////////////////////////////////////////////////////////
// SubmitFeedbackView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.feedback;

import granite.feedback.service.requestreply.CreateFeedbackData;
import granite.feedback.service.requestreply.CreateFeedbackReply;
import granite.feedback.service.requestreply.CreateFeedbackRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.FeedbackInput;
import granite.frontendcontroller.service.requestreply.FeedbackOutput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SubmitFeedbackView
    implements IInputAction<FeedbackInput,FeedbackOutput>
{
    public static final int MAX_DETAIL_LENGTH = 4000;

    private final GraniteHelpServiceClients clients;
    private final SessionRequestContext     context;

    public
    SubmitFeedbackView(GraniteHelpServiceClients clients,SessionRequestContext context)
    {
        this.clients = clients;
        this.context = context;
    }

    @Override
    public CompletionStage<FeedbackOutput>
    apply(FeedbackInput input)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return CompletableFuture.completedFuture(describe(input,UnauthenticatedFeedbackOutputs.feedback()));

        List<String> problems = validate(input);

        if (!problems.isEmpty())
            return CompletableFuture.completedFuture(describe(input,new FeedbackOutput().setFailureMessages(problems)));

        return
            clients
                .getFeedbackCommandClient()
                .createFeedback(
                    new CreateFeedbackRequest()
                        .setFeedback(
                            new CreateFeedbackData()
                                .setUserId(caller.get())
                                .setType(input.getType())
                                .setSummary(input.getSummary().trim())
                                .setDetails(trimmed(input.getDetails()))))
                .thenApply(reply -> describe(input,toOutput(reply)));
    }

    protected List<String>
    validate(FeedbackInput input)
    {
        List<String> problems = new ArrayList<>();

        if (input.getType() == null)
            problems.add("type is required");

        if (input.getSummary() == null || input.getSummary().isBlank())
            problems.add("summary is required");

        if (input.getDetails() != null)
            for (Map.Entry<String,String> entry : input.getDetails().entrySet())
                if (entry.getValue() != null && entry.getValue().length() > MAX_DETAIL_LENGTH)
                    problems.add(entry.getKey() + " is longer than " + MAX_DETAIL_LENGTH + " characters");

        return problems;
    }

    private FeedbackOutput
    toOutput(CreateFeedbackReply reply)
    {
        if (reply != null && reply.isSuccess())
            return new FeedbackOutput().setSubmitted(reply.getCreatedFeedback());

        String reason = reply == null || reply.getFailureMessage() == null ? "unknown error" : reply.getFailureMessage();

        return new FeedbackOutput().setFailureMessages(new ArrayList<>(List.of("Unable to submit feedback: " + reason)));
    }

    private Map<String,String>
    trimmed(Map<String,String> details)
    {
        Map<String,String> result = new LinkedHashMap<>();

        if (details != null)
            for (Map.Entry<String,String> entry : details.entrySet())
                if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isBlank())
                    result.put(entry.getKey(),entry.getValue().trim());

        return result;
    }

    private FeedbackOutput
    describe(FeedbackInput input,FeedbackOutput output)
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
