//////////////////////////////////////////////////////////////////////////////
// SurveyReviewLoader.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.survey;

import granite.survey.service.requestreply.FindAllSurveyResponsesReply;
import granite.survey.service.requestreply.FindAllSurveyResponsesRequest;
import granite.survey.service.requestreply.GraniteSurveyQuestions;
import granite.survey.service.requestreply.SurveyResponseData;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.service.requestreply.SurveyReviewOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeySetReply;
import granite.user.service.requestreply.FindUsersByKeySetRequest;
import granite.user.service.requestreply.UserData;
import granite.user.service.requestreply.UserKeySet;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SurveyReviewLoader
{
    private final GraniteHelpServiceClients clients;
    private final IUserQueryServiceClient   userQueryClient;

    public
    SurveyReviewLoader(GraniteHelpServiceClients clients,IUserQueryServiceClient userQueryClient)
    {
        this.clients         = clients;
        this.userQueryClient = userQueryClient;
    }

    public CompletionStage<SurveyReviewOutput>
    load(List<String> failures)
    {
        return
            clients
                .getSurveyQueryClient()
                .findAllSurveyResponses(new FindAllSurveyResponsesRequest())
                .thenCompose(reply -> withUserNames(reply,failures));
    }

    protected CompletionStage<SurveyReviewOutput>
    withUserNames(FindAllSurveyResponsesReply reply,List<String> failures)
    {
        if (reply == null || !reply.isSuccess() || reply.getResponses() == null)
        {
            failures.add("Unable to refresh survey responses: " + reason(reply == null ? null : reply.getFailureMessage()));
            return CompletableFuture.completedFuture(new SurveyReviewOutput().setFailureMessages(failures));
        }

        List<SurveyResponseData> responses = reply.getResponses();
        Set<Long>                userIds   = new LinkedHashSet<>();

        for (SurveyResponseData item : responses)
            if (item.getUserId() != null)
                userIds.add(item.getUserId());

        if (userIds.isEmpty())
            return CompletableFuture.completedFuture(output(responses,new LinkedHashMap<>(),failures));

        return
            userQueryClient
                .findUsersByKeySet(new FindUsersByKeySetRequest().setKeys(UserKeySet.ofIds(userIds)).setReturnIfFound(true))
                .thenApply(users -> output(responses,userNames(users,failures),failures));
    }

    private Map<Long,String>
    userNames(FindUsersByKeySetReply reply,List<String> failures)
    {
        Map<Long,String> names = new LinkedHashMap<>();

        if (reply == null || !reply.isSuccess())
        {
            failures.add("Unable to resolve user names: " + reason(reply == null ? null : reply.getFailureMessage()));
            return names;
        }

        for (UserData user : reply.getFoundUsers() == null ? List.<UserData>of() : reply.getFoundUsers())
            names.put(user.getUserId(),user.getUserName());

        return names;
    }

    private SurveyReviewOutput
    output(List<SurveyResponseData> responses,Map<Long,String> names,List<String> failures)
    {
        return
            new SurveyReviewOutput()
                .setResponses(responses)
                .setAverages(averages(responses))
                .setUserNames(names)
                .setFailureMessages(failures);
    }

    public static Map<String,Double>
    averages(List<SurveyResponseData> responses)
    {
        Map<String,Double> result = new LinkedHashMap<>();

        for (String key : GraniteSurveyQuestions.keys())
        {
            double sum   = 0;
            int    count = 0;

            for (SurveyResponseData response : responses)
            {
                Integer answer = response.getAnswers() == null ? null : response.getAnswers().get(key);

                if (answer != null)
                {
                    sum += answer;
                    count++;
                }
            }

            if (count > 0)
                result.put(key,Math.round(sum / count * 100.0) / 100.0);
        }

        return result;
    }

    private String
    reason(String message)
    {
        return message == null || message.isBlank() ? "unknown error" : message;
    }
}

//////////////////////////////////////////////////////////////////////////////
