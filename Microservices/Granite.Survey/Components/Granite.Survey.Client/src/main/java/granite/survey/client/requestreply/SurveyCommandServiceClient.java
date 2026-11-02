//////////////////////////////////////////////////////////////////////////////
// SurveyCommandServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.client.requestreply;

import granite.survey.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

public
class SurveyCommandServiceClient
    extends RestEasyRestClient
    implements ISurveyCommandServiceClient
{
    public
    SurveyCommandServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "survey-command-service/");
    }

    @Override
    public CompletionStage<SubmitSurveyResponseReply>
    submitSurveyResponse(SubmitSurveyResponseRequest request)
    {
        return
            doPostAsync(
                "submit-survey-response",
                SubmitSurveyResponseReply.class,
                request);
    }

    @Override
    public CompletionStage<UpdateSurveyResponsesReply>
    updateSurveyResponses(UpdateSurveyResponsesRequest request)
    {
        return
            doPostAsync(
                "update-survey-responses",
                UpdateSurveyResponsesReply.class,
                request);
    }

    @Override
    public CompletionStage<MarkSurveyResponsesSentReply>
    markSurveyResponsesSent(MarkSurveyResponsesSentRequest request)
    {
        return
            doPostAsync(
                "mark-survey-responses-sent",
                MarkSurveyResponsesSentReply.class,
                request);
    }

    @Override
    public CompletionStage<MarkSurveyResponsesReceivedReply>
    markSurveyResponsesReceived(MarkSurveyResponsesReceivedRequest request)
    {
        return
            doPostAsync(
                "mark-survey-responses-received",
                MarkSurveyResponsesReceivedReply.class,
                request);
    }

    public SurveyCommandServiceClient
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
