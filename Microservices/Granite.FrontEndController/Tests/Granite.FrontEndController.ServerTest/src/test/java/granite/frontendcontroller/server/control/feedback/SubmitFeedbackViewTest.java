//////////////////////////////////////////////////////////////////////////////
// SubmitFeedbackViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.feedback;

import granite.feedback.client.requestreply.IFeedbackCommandServiceClient;
import granite.feedback.service.requestreply.CreateFeedbackReply;
import granite.feedback.service.requestreply.CreateFeedbackRequest;
import granite.feedback.service.requestreply.FeedbackData;
import granite.feedback.service.requestreply.FeedbackStatus;
import granite.feedback.service.requestreply.FeedbackType;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.FeedbackInput;
import granite.frontendcontroller.service.requestreply.FeedbackOutput;
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
class SubmitFeedbackViewTest
{
    private IFeedbackCommandServiceClient commandClient;
    private GraniteHelpServiceClients     clients;

    @BeforeEach
    public void
    setUp()
    {
        commandClient = Mockito.mock(IFeedbackCommandServiceClient.class);
        clients       = new GraniteHelpServiceClients().setFeedbackCommandClient(() -> commandClient);

        Mockito
            .when(commandClient.createFeedback(Mockito.any(CreateFeedbackRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new CreateFeedbackReply()
                        .setSuccess(true)
                        .setCreatedFeedback(new FeedbackData().setFeedbackId(5L).setStatus(FeedbackStatus.NEW))));
    }

    @Test
    public void
    stampsTheVerifiedCallerNotThePayloadUserId()
        throws Exception
    {
        FeedbackOutput output = run(7L,input().setUserId(99L));

        Mockito.verify(commandClient).createFeedback(
            Mockito.argThat(r ->
                r.getFeedback().getUserId().equals(7L) &&
                r.getFeedback().getType() == FeedbackType.DEFECT &&
                r.getFeedback().getSummary().equals("Broken save") &&
                r.getFeedback().getDetails().equals(Map.of("actualBehavior","Nothing"))));
        assertEquals(5L,output.getSubmitted().getFeedbackId());
        assertTrue(output.getFailureMessages().isEmpty());
        assertEquals(7L,output.getUserId());
    }

    @Test
    public void
    rejectsMissingSummaryWithoutCallingFeedback()
        throws Exception
    {
        FeedbackOutput output = run(7L,input().setSummary(" "));

        Mockito.verifyNoInteractions(commandClient);
        assertEquals(List.of("summary is required"),output.getFailureMessages());
        assertNull(output.getSubmitted());
    }

    @Test
    public void
    reportsServiceFailure()
        throws Exception
    {
        Mockito
            .when(commandClient.createFeedback(Mockito.any(CreateFeedbackRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new CreateFeedbackReply().setSuccess(false).setFailureMessage("db down")));

        FeedbackOutput output = run(7L,input());

        assertEquals(List.of("Unable to submit feedback: db down"),output.getFailureMessages());
    }

    @Test
    public void
    refusesWithoutVerifiedCaller()
        throws Exception
    {
        FeedbackOutput output = run(null,input());

        Mockito.verifyNoInteractions(commandClient);
        assertEquals(List.of(UnauthenticatedFeedbackOutputs.MESSAGE),output.getFailureMessages());
    }

    private FeedbackInput
    input()
    {
        return
            new FeedbackInput()
                .setType(FeedbackType.DEFECT)
                .setSummary("Broken save ")
                .setDetails(Map.of("actualBehavior"," Nothing ","expectedBehavior"," "))
                .setSource("Feedback.New")
                .setCommand("Submit")
                .setDestination("FeedbackView");
    }

    private FeedbackOutput
    run(Long caller,FeedbackInput input)
        throws Exception
    {
        return
            new SubmitFeedbackView(clients,SessionTestSupport.context("d",caller == null ? null : 11L,caller))
                .apply(input)
                .toCompletableFuture()
                .get();
    }
}

//////////////////////////////////////////////////////////////////////////////
