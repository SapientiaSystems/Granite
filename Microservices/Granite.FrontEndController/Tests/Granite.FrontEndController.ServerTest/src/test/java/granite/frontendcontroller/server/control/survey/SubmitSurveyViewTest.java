//////////////////////////////////////////////////////////////////////////////
// SubmitSurveyViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.survey;

import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.SurveyInput;
import granite.frontendcontroller.service.requestreply.SurveyOutput;
import granite.survey.client.requestreply.ISurveyCommandServiceClient;
import granite.survey.service.requestreply.SubmitSurveyResponseReply;
import granite.survey.service.requestreply.SubmitSurveyResponseRequest;
import granite.survey.service.requestreply.SurveyResponseData;
import granite.survey.service.requestreply.SurveyResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SubmitSurveyViewTest
{
    private ISurveyCommandServiceClient commandClient;
    private GraniteHelpServiceClients   clients;

    @BeforeEach
    public void
    setUp()
    {
        commandClient = Mockito.mock(ISurveyCommandServiceClient.class);
        clients       = new GraniteHelpServiceClients().setSurveyCommandClient(() -> commandClient);

        Mockito
            .when(commandClient.submitSurveyResponse(Mockito.any(SubmitSurveyResponseRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new SubmitSurveyResponseReply()
                        .setSuccess(true)
                        .setSubmittedResponse(new SurveyResponseData().setResponseId(5L).setStatus(SurveyResponseStatus.NEW))));
    }

    @Test
    public void
    stampsTheVerifiedCallerNotThePayloadUserId()
        throws Exception
    {
        SurveyOutput output = run(7L,input(Map.of("satisfied",4,"needs",3,"recommend",5)).setUserId(99L));

        Mockito.verify(commandClient).submitSurveyResponse(
            Mockito.argThat(r ->
                r.getResponse().getUserId().equals(7L) &&
                r.getResponse().getAnswers().get("recommend").equals(5) &&
                r.getResponse().getComment().equals("Nice")));
        assertEquals(5L,output.getSubmitted().getResponseId());
        assertTrue(output.getFailureMessages().isEmpty());
        assertEquals(7L,output.getUserId());
        assertEquals(3,output.getQuestions().size());
    }

    @Test
    public void
    rejectsMissingAndOutOfRangeAnswersWithoutCallingSurvey()
        throws Exception
    {
        SurveyOutput output = run(7L,input(Map.of("satisfied",9,"other",1)));

        Mockito.verifyNoInteractions(commandClient);
        assertEquals(
            List.of(
                "satisfied must be between 1 and 5",
                "needs is required",
                "recommend is required",
                "other is not a survey question"),
            output.getFailureMessages());
        assertNull(output.getSubmitted());
    }

    @Test
    public void
    reportsServiceFailure()
        throws Exception
    {
        Mockito
            .when(commandClient.submitSurveyResponse(Mockito.any(SubmitSurveyResponseRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new SubmitSurveyResponseReply().setSuccess(false).setFailureMessage("db down")));

        SurveyOutput output = run(7L,input(Map.of("satisfied",4,"needs",3,"recommend",5)));

        assertEquals(List.of("Unable to submit survey: db down"),output.getFailureMessages());
    }

    @Test
    public void
    refusesWithoutVerifiedCaller()
        throws Exception
    {
        SurveyOutput output = run(null,input(Map.of("satisfied",4,"needs",3,"recommend",5)));

        Mockito.verifyNoInteractions(commandClient);
        assertEquals(List.of(UnauthenticatedSurveyOutputs.MESSAGE),output.getFailureMessages());
    }

    private SurveyInput
    input(Map<String,Integer> answers)
    {
        return
            (SurveyInput)new SurveyInput()
                .setAnswers(answers)
                .setComment(" Nice ")
                .setSource("Survey.New")
                .setCommand("Submit")
                .setDestination("SurveyView");
    }

    private SurveyOutput
    run(Long caller,SurveyInput input)
        throws Exception
    {
        return
            new SubmitSurveyView(clients,SessionTestSupport.context("d",caller == null ? null : 11L,caller))
                .apply(input)
                .toCompletableFuture()
                .get();
    }
}

//////////////////////////////////////////////////////////////////////////////
