//////////////////////////////////////////////////////////////////////////////
// FeedbackReviewViewsTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.feedback;

import granite.feedback.client.requestreply.IFeedbackCommandServiceClient;
import granite.feedback.client.requestreply.IFeedbackQueryServiceClient;
import granite.feedback.service.requestreply.FeedbackData;
import granite.feedback.service.requestreply.FeedbackStatus;
import granite.feedback.service.requestreply.FindAllFeedbackReply;
import granite.feedback.service.requestreply.FindAllFeedbackRequest;
import granite.feedback.service.requestreply.UpdateFeedbackData;
import granite.feedback.service.requestreply.UpdateFeedbacksReply;
import granite.feedback.service.requestreply.UpdateFeedbacksRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.FeedbackReviewInput;
import granite.frontendcontroller.service.requestreply.FeedbackReviewOutput;
import granite.frontendcontroller.service.requestreply.GetFeedbackReviewOutputRequest;
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
class FeedbackReviewViewsTest
{
    private IFeedbackCommandServiceClient commandClient;
    private IFeedbackQueryServiceClient   queryClient;
    private IUserQueryServiceClient       userQueryClient;
    private GraniteHelpServiceClients     clients;

    @BeforeEach
    public void
    setUp()
    {
        commandClient   = Mockito.mock(IFeedbackCommandServiceClient.class);
        queryClient     = Mockito.mock(IFeedbackQueryServiceClient.class);
        userQueryClient = Mockito.mock(IUserQueryServiceClient.class);
        clients         =
            new GraniteHelpServiceClients()
                .setFeedbackCommandClient(() -> commandClient)
                .setFeedbackQueryClient(() -> queryClient);

        Mockito
            .when(queryClient.findAllFeedback(Mockito.any(FindAllFeedbackRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllFeedbackReply()
                        .setSuccess(true)
                        .setFeedback(List.of(new FeedbackData().setFeedbackId(5L).setUserId(7L).setStatus(FeedbackStatus.NEW)))));
        Mockito
            .when(userQueryClient.findUsersByKeySet(Mockito.any(FindUsersByKeySetRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindUsersByKeySetReply().setSuccess(true).setFoundUsers(List.of(new UserData().setUserId(7L).setUserName("alice")))));
        Mockito
            .when(commandClient.updateFeedbacks(Mockito.any(UpdateFeedbacksRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new UpdateFeedbacksReply().setSuccess(true)));
    }

    @Test
    public void
    listsFeedbackWithUserNames()
        throws Exception
    {
        FeedbackReviewOutput output =
            new InitializeFeedbackReviewView(loader(),context(7L))
                .apply((GetFeedbackReviewOutputRequest)new GetFeedbackReviewOutputRequest().setSource("Feedback.All").setDestination("FeedbackReviewView"))
                .toCompletableFuture()
                .get();

        assertEquals(1,output.getFeedback().size());
        assertEquals(Map.of(7L,"alice"),output.getUserNames());
        assertTrue(output.getFailureMessages().isEmpty());
        assertEquals("FeedbackReviewView",output.getDestination());
    }

    @Test
    public void
    savesChangesAndReportsFailedIds()
        throws Exception
    {
        Mockito
            .when(commandClient.updateFeedbacks(Mockito.any(UpdateFeedbacksRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new UpdateFeedbacksReply().setSuccess(false).setFailedFeedbackIds(new TreeSet<>(Set.of(6L)))));

        FeedbackReviewOutput output =
            new SaveFeedbackReviewView(clients,loader(),context(7L))
                .apply(
                    new FeedbackReviewInput()
                        .setChanges(
                            List.of(
                                new UpdateFeedbackData().setFeedbackId(5L).setStatus(FeedbackStatus.SENT).setNotes("forwarded"),
                                new UpdateFeedbackData().setFeedbackId(6L).setNotes("gone")))
                        .setSource("Feedback.All")
                        .setCommand("Save")
                        .setDestination("FeedbackReviewView"))
                .toCompletableFuture()
                .get();

        Mockito.verify(commandClient).updateFeedbacks(Mockito.argThat(r -> r.getFeedback().size() == 2));
        assertEquals(List.of("6: not saved"),output.getFailureMessages());
        assertEquals(1,output.getFeedback().size());
    }

    @Test
    public void
    refusesWithoutVerifiedCaller()
        throws Exception
    {
        FeedbackReviewOutput output =
            new SaveFeedbackReviewView(clients,loader(),context(null))
                .apply(new FeedbackReviewInput().setSource("Feedback.All").setCommand("Save").setDestination("FeedbackReviewView"))
                .toCompletableFuture()
                .get();

        assertEquals(List.of(UnauthenticatedFeedbackOutputs.MESSAGE),output.getFailureMessages());
        Mockito.verifyNoInteractions(commandClient,queryClient,userQueryClient);
    }

    private FeedbackReviewLoader
    loader()
    {
        return new FeedbackReviewLoader(clients,userQueryClient);
    }

    private SessionRequestContext
    context(Long caller)
    {
        return SessionTestSupport.context("d",caller == null ? null : 11L,caller);
    }
}

//////////////////////////////////////////////////////////////////////////////
