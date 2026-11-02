//////////////////////////////////////////////////////////////////////////////
// SurveyReviewViewsTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.survey;

import granite.survey.client.requestreply.ISurveyCommandServiceClient;
import granite.survey.client.requestreply.ISurveyQueryServiceClient;
import granite.survey.service.requestreply.FindAllSurveyResponsesReply;
import granite.survey.service.requestreply.FindAllSurveyResponsesRequest;
import granite.survey.service.requestreply.SurveyResponseData;
import granite.survey.service.requestreply.SurveyResponseStatus;
import granite.survey.service.requestreply.UpdateSurveyResponseData;
import granite.survey.service.requestreply.UpdateSurveyResponsesReply;
import granite.survey.service.requestreply.UpdateSurveyResponsesRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SurveyReviewInput;
import granite.frontendcontroller.service.requestreply.SurveyReviewOutput;
import granite.frontendcontroller.service.requestreply.GetSurveyReviewOutputRequest;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeySetReply;
import granite.user.service.requestreply.FindUsersByKeySetRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SurveyReviewViewsTest
{
    private ISurveyCommandServiceClient commandClient;
    private ISurveyQueryServiceClient   queryClient;
    private IUserQueryServiceClient       userQueryClient;
    private GraniteHelpServiceClients     clients;

    @BeforeEach
    public void
    setUp()
    {
        commandClient   = Mockito.mock(ISurveyCommandServiceClient.class);
        queryClient     = Mockito.mock(ISurveyQueryServiceClient.class);
        userQueryClient = Mockito.mock(IUserQueryServiceClient.class);
        clients         =
            new GraniteHelpServiceClients()
                .setSurveyCommandClient(() -> commandClient)
                .setSurveyQueryClient(() -> queryClient);

        Mockito
            .when(queryClient.findAllSurveyResponses(Mockito.any(FindAllSurveyResponsesRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllSurveyResponsesReply()
                        .setSuccess(true)
                        .setResponses(
                            List.of(
                                new SurveyResponseData().setResponseId(5L).setUserId(7L).setStatus(SurveyResponseStatus.NEW).setAnswers(Map.of("satisfied",4,"needs",2,"recommend",5)),
                                new SurveyResponseData().setResponseId(4L).setUserId(7L).setStatus(SurveyResponseStatus.NEW).setAnswers(Map.of("satisfied",3,"needs",3,"recommend",4))))));
        Mockito
            .when(userQueryClient.findUsersByKeySet(Mockito.any(FindUsersByKeySetRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindUsersByKeySetReply().setSuccess(true).setFoundUsers(List.of(new UserData().setUserId(7L).setUserName("alice")))));
        Mockito
            .when(commandClient.updateSurveyResponses(Mockito.any(UpdateSurveyResponsesRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new UpdateSurveyResponsesReply().setSuccess(true)));
    }

    @Test
    public void
    listsResponsesWithUserNamesAndAverages()
        throws Exception
    {
        SurveyReviewOutput output =
            new InitializeSurveyReviewView(loader(),context(7L))
                .apply((GetSurveyReviewOutputRequest)new GetSurveyReviewOutputRequest().setSource("Survey.All").setDestination("SurveyReviewView"))
                .toCompletableFuture()
                .get();

        assertEquals(2,output.getResponses().size());
        assertEquals(Map.of(7L,"alice"),output.getUserNames());
        assertEquals(Map.of("satisfied",3.5,"needs",2.5,"recommend",4.5),output.getAverages());
        assertEquals(3,output.getQuestions().size());
        assertTrue(output.getFailureMessages().isEmpty());
        assertEquals("SurveyReviewView",output.getDestination());
    }

    @Test
    public void
    savesChangesAndReportsFailedIds()
        throws Exception
    {
        Mockito
            .when(commandClient.updateSurveyResponses(Mockito.any(UpdateSurveyResponsesRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new UpdateSurveyResponsesReply().setSuccess(false).setFailedResponseIds(new TreeSet<>(Set.of(6L)))));

        SurveyReviewOutput output =
            new SaveSurveyReviewView(clients,loader(),context(7L))
                .apply(
                    new SurveyReviewInput()
                        .setChanges(
                            List.of(
                                new UpdateSurveyResponseData().setResponseId(5L).setStatus(SurveyResponseStatus.SENT).setNotes("forwarded"),
                                new UpdateSurveyResponseData().setResponseId(6L).setNotes("gone")))
                        .setSource("Survey.All")
                        .setCommand("Save")
                        .setDestination("SurveyReviewView"))
                .toCompletableFuture()
                .get();

        Mockito.verify(commandClient).updateSurveyResponses(Mockito.argThat(r -> r.getResponses().size() == 2));
        assertEquals(List.of("6: not saved"),output.getFailureMessages());
        assertEquals(2,output.getResponses().size());
    }

    @Test
    public void
    refusesWithoutVerifiedCaller()
        throws Exception
    {
        SurveyReviewOutput output =
            new SaveSurveyReviewView(clients,loader(),context(null))
                .apply(new SurveyReviewInput().setSource("Survey.All").setCommand("Save").setDestination("SurveyReviewView"))
                .toCompletableFuture()
                .get();

        assertEquals(List.of(UnauthenticatedSurveyOutputs.MESSAGE),output.getFailureMessages());
        Mockito.verifyNoInteractions(commandClient,queryClient,userQueryClient);
    }

    private SurveyReviewLoader
    loader()
    {
        return new SurveyReviewLoader(clients,userQueryClient);
    }

    private SessionRequestContext
    context(Long caller)
    {
        return SessionTestSupport.context("d",caller == null ? null : 11L,caller);
    }
}

//////////////////////////////////////////////////////////////////////////////
