//////////////////////////////////////////////////////////////////////////////
// SurveyQueryServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.client.requestreply;

import granite.survey.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

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

    @Override
    public void
    close() {}

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
