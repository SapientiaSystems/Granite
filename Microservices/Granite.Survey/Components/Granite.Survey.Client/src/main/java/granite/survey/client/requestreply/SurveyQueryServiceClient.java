//////////////////////////////////////////////////////////////////////////////
// SurveyQueryServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.client.requestreply;

import granite.survey.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.core.service.CountedClient;
import strata.client.core.service.StandardResponseProcessor;
import strata.client.resteasy.service.RestEasyRestClient;
import strata.foundation.core.utility.BasicRetryExecutor;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

public
class SurveyQueryServiceClient
    extends RestEasyRestClient
    implements ISurveyQueryServiceClient
{
    public
    SurveyQueryServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "survey-query-service/");
    }

    public
    SurveyQueryServiceClient(CountedClient client,String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "survey-query-service/");
    }

    public
    SurveyQueryServiceClient(CountedClient client,ExecutorService executor,String baseUrl)
    {
        super(
            client,
            executor,
            false,
            preprocessBaseUrl(baseUrl),
            "survey-query-service/",
            new StandardResponseProcessor(),
            new BasicRetryExecutor());
    }

    @Override
    public CompletionStage<FindSurveyResponsesByIdsReply>
    findSurveyResponsesByIds(FindSurveyResponsesByIdsRequest request)
    {
        return
            doPostAsync(
                "find-survey-responses-by-ids",
                FindSurveyResponsesByIdsReply.class,
                request);
    }

    @Override
    public CompletionStage<FindAllSurveyResponsesReply>
    findAllSurveyResponses(FindAllSurveyResponsesRequest request)
    {
        return
            doPostAsync(
                "find-all-survey-responses",
                FindAllSurveyResponsesReply.class,
                request);
    }

    public SurveyQueryServiceClient
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
