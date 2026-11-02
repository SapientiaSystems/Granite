//////////////////////////////////////////////////////////////////////////////
// SubmitSurveyView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.survey;

import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SurveyInput;
import granite.frontendcontroller.service.requestreply.SurveyOutput;
import granite.survey.service.requestreply.GraniteSurveyQuestions;
import granite.survey.service.requestreply.SubmitSurveyResponseData;
import granite.survey.service.requestreply.SubmitSurveyResponseReply;
import granite.survey.service.requestreply.SubmitSurveyResponseRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SubmitSurveyView
    implements IInputAction<SurveyInput,SurveyOutput>
{
    private final GraniteHelpServiceClients clients;
    private final SessionRequestContext     context;

    public
    SubmitSurveyView(GraniteHelpServiceClients clients,SessionRequestContext context)
    {
        this.clients = clients;
        this.context = context;
    }

    @Override
    public CompletionStage<SurveyOutput>
    apply(SurveyInput input)
    {
        Optional<Long> caller = context.getCallerUserId();

        if (caller.isEmpty())
            return CompletableFuture.completedFuture(describe(input,UnauthenticatedSurveyOutputs.survey()));

        List<String> problems = validate(input);

        if (!problems.isEmpty())
            return CompletableFuture.completedFuture(describe(input,new SurveyOutput().setFailureMessages(problems)));

        return
            clients
                .getSurveyCommandClient()
                .submitSurveyResponse(
                    new SubmitSurveyResponseRequest()
                        .setResponse(
                            new SubmitSurveyResponseData()
                                .setUserId(caller.get())
                                .setAnswers(new LinkedHashMap<>(input.getAnswers()))
                                .setComment(trimmed(input.getComment()))))
                .thenApply(reply -> describe(input,toOutput(reply)));
    }

    protected List<String>
    validate(SurveyInput input)
    {
        List<String>        problems = new ArrayList<>();
        Map<String,Integer> answers  = input.getAnswers() == null ? Map.of() : input.getAnswers();
        Set<String>         keys     = new TreeSet<>(GraniteSurveyQuestions.keys());

        for (String key : GraniteSurveyQuestions.keys())
        {
            Integer answer = answers.get(key);

            if (answer == null)
                problems.add(key + " is required");
            else if (answer < GraniteSurveyQuestions.MIN_ANSWER || answer > GraniteSurveyQuestions.MAX_ANSWER)
                problems.add(key + " must be between " + GraniteSurveyQuestions.MIN_ANSWER + " and " + GraniteSurveyQuestions.MAX_ANSWER);
        }

        for (String key : answers.keySet())
            if (!keys.contains(key))
                problems.add(key + " is not a survey question");

        if (input.getComment() != null && input.getComment().length() > GraniteSurveyQuestions.MAX_COMMENT_LENGTH)
            problems.add("comment is longer than " + GraniteSurveyQuestions.MAX_COMMENT_LENGTH + " characters");

        return problems;
    }

    private SurveyOutput
    toOutput(SubmitSurveyResponseReply reply)
    {
        if (reply != null && reply.isSuccess())
            return new SurveyOutput().setSubmitted(reply.getSubmittedResponse());

        String reason = reply == null || reply.getFailureMessage() == null ? "unknown error" : reply.getFailureMessage();

        return new SurveyOutput().setFailureMessages(new ArrayList<>(List.of("Unable to submit survey: " + reason)));
    }

    private String
    trimmed(String comment)
    {
        return comment == null || comment.isBlank() ? null : comment.trim();
    }

    private SurveyOutput
    describe(SurveyInput input,SurveyOutput output)
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
