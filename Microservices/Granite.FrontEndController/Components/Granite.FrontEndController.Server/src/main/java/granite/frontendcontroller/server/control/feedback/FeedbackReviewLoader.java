//////////////////////////////////////////////////////////////////////////////
// FeedbackReviewLoader.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.feedback;

import granite.feedback.service.requestreply.FeedbackData;
import granite.feedback.service.requestreply.FindAllFeedbackReply;
import granite.feedback.service.requestreply.FindAllFeedbackRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.service.requestreply.FeedbackReviewOutput;
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
class FeedbackReviewLoader
{
    private final GraniteHelpServiceClients clients;
    private final IUserQueryServiceClient   userQueryClient;

    public
    FeedbackReviewLoader(GraniteHelpServiceClients clients,IUserQueryServiceClient userQueryClient)
    {
        this.clients         = clients;
        this.userQueryClient = userQueryClient;
    }

    public CompletionStage<FeedbackReviewOutput>
    load(List<String> failures)
    {
        return
            clients
                .getFeedbackQueryClient()
                .findAllFeedback(new FindAllFeedbackRequest())
                .thenCompose(reply -> withUserNames(reply,failures));
    }

    protected CompletionStage<FeedbackReviewOutput>
    withUserNames(FindAllFeedbackReply reply,List<String> failures)
    {
        if (reply == null || !reply.isSuccess() || reply.getFeedback() == null)
        {
            failures.add("Unable to refresh feedback list: " + reason(reply == null ? null : reply.getFailureMessage()));
            return CompletableFuture.completedFuture(new FeedbackReviewOutput().setFailureMessages(failures));
        }

        List<FeedbackData> feedback = reply.getFeedback();
        Set<Long>          userIds  = new LinkedHashSet<>();

        for (FeedbackData item : feedback)
            if (item.getUserId() != null)
                userIds.add(item.getUserId());

        if (userIds.isEmpty())
            return CompletableFuture.completedFuture(output(feedback,new LinkedHashMap<>(),failures));

        return
            userQueryClient
                .findUsersByKeySet(new FindUsersByKeySetRequest().setKeys(UserKeySet.ofIds(userIds)).setReturnIfFound(true))
                .thenApply(users -> output(feedback,userNames(users,failures),failures));
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

    private FeedbackReviewOutput
    output(List<FeedbackData> feedback,Map<Long,String> names,List<String> failures)
    {
        return
            new FeedbackReviewOutput()
                .setFeedback(feedback)
                .setUserNames(names)
                .setFailureMessages(failures);
    }

    private String
    reason(String message)
    {
        return message == null || message.isBlank() ? "unknown error" : message;
    }
}

//////////////////////////////////////////////////////////////////////////////
