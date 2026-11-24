//////////////////////////////////////////////////////////////////////////////
// FeedbackCommandServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.client.requestreply;

import granite.feedback.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.core.service.CountedClient;
import strata.client.core.service.StandardResponseProcessor;
import strata.client.resteasy.service.RestEasyRestClient;
import strata.foundation.core.utility.BasicRetryExecutor;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

public
class FeedbackCommandServiceClient
    extends RestEasyRestClient
    implements IFeedbackCommandServiceClient
{
    public
    FeedbackCommandServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "feedback-command-service/");
    }

    public
    FeedbackCommandServiceClient(CountedClient client,String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "feedback-command-service/");
    }

    public
    FeedbackCommandServiceClient(CountedClient client,ExecutorService executor,String baseUrl)
    {
        super(
            client,
            executor,
            false,
            preprocessBaseUrl(baseUrl),
            "feedback-command-service/",
            new StandardResponseProcessor(),
            new BasicRetryExecutor());
    }

    @Override
    public CompletionStage<CreateFeedbackReply>
    createFeedback(CreateFeedbackRequest request)
    {
        return
            doPostAsync(
                "create-feedback",
                CreateFeedbackReply.class,
                request);
    }

    @Override
    public CompletionStage<UpdateFeedbacksReply>
    updateFeedbacks(UpdateFeedbacksRequest request)
    {
        return
            doPostAsync(
                "update-feedbacks",
                UpdateFeedbacksReply.class,
                request);
    }

    @Override
    public CompletionStage<MarkFeedbackSentReply>
    markFeedbackSent(MarkFeedbackSentRequest request)
    {
        return
            doPostAsync(
                "mark-feedback-sent",
                MarkFeedbackSentReply.class,
                request);
    }

    @Override
    public CompletionStage<MarkFeedbackReceivedReply>
    markFeedbackReceived(MarkFeedbackReceivedRequest request)
    {
        return
            doPostAsync(
                "mark-feedback-received",
                MarkFeedbackReceivedReply.class,
                request);
    }

    public FeedbackCommandServiceClient
    setAuthorizationHeader(String accessToken)
    {
        setHeader("Authorization","Bearer " + accessToken);
        return this;
    }

    private static String
    preprocessBaseUrl(String baseUrl)
    {
        return
            baseUrl.endsWith("/")
                ? baseUrl
                : baseUrl + '/';
    }
}

//////////////////////////////////////////////////////////////////////////////
