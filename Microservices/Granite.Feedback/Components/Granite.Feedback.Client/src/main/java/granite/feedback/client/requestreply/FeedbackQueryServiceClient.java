//////////////////////////////////////////////////////////////////////////////
// FeedbackQueryServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.client.requestreply;

import granite.feedback.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.core.service.CountedClient;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

public
class FeedbackQueryServiceClient
    extends RestEasyRestClient
    implements IFeedbackQueryServiceClient
{
    public
    FeedbackQueryServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "feedback-query-service/");
    }

    public
    FeedbackQueryServiceClient(CountedClient client,String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "feedback-query-service/");
    }

    @Override
    public CompletionStage<FindFeedbackByIdsReply>
    findFeedbackByIds(FindFeedbackByIdsRequest request)
    {
        return
            doPostAsync(
                "find-feedback-by-ids",
                FindFeedbackByIdsReply.class,
                request);
    }

    @Override
    public CompletionStage<FindAllFeedbackReply>
    findAllFeedback(FindAllFeedbackRequest request)
    {
        return
            doPostAsync(
                "find-all-feedback",
                FindAllFeedbackReply.class,
                request);
    }

    public FeedbackQueryServiceClient
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
