//////////////////////////////////////////////////////////////////////////////
// HomeViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.home;

import granite.feedback.client.requestreply.IFeedbackQueryServiceClient;
import granite.feedback.service.requestreply.FeedbackData;
import granite.feedback.service.requestreply.FeedbackStatus;
import granite.feedback.service.requestreply.FeedbackType;
import granite.feedback.service.requestreply.FindAllFeedbackReply;
import granite.feedback.service.requestreply.FindAllFeedbackRequest;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.GetHomeOutputRequest;
import granite.frontendcontroller.service.requestreply.HomeOutput;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.FindSessionsReply;
import granite.session.service.requestreply.FindSessionsRequest;
import granite.session.service.requestreply.SessionData;
import granite.session.service.requestreply.SessionLoginMethod;
import granite.survey.client.requestreply.ISurveyQueryServiceClient;
import granite.survey.service.requestreply.FindAllSurveyResponsesReply;
import granite.survey.service.requestreply.FindAllSurveyResponsesRequest;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class HomeViewTest
{
    private static final Instant NOW = Instant.parse("2026-11-02T18:00:00Z");

    private IUserQueryServiceClient     userQueryClient;
    private ISessionQueryServiceClient  sessionQueryClient;
    private IFeedbackQueryServiceClient feedbackQueryClient;
    private ISurveyQueryServiceClient   surveyQueryClient;
    private GraniteHelpServiceClients   clients;

    @BeforeEach
    public void
    setUp()
    {
        userQueryClient     = Mockito.mock(IUserQueryServiceClient.class);
        sessionQueryClient  = Mockito.mock(ISessionQueryServiceClient.class);
        feedbackQueryClient = Mockito.mock(IFeedbackQueryServiceClient.class);
        surveyQueryClient   = Mockito.mock(ISurveyQueryServiceClient.class);
        clients             =
            new GraniteHelpServiceClients()
                .setFeedbackQueryClient(() -> feedbackQueryClient)
                .setSurveyQueryClient(() -> surveyQueryClient);

        Mockito
            .when(userQueryClient.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllUsersReply()
                        .setSuccess(true)
                        .setUsers(
                            List.of(
                                new UserData().setUserId(7L).setUserName("alice").setAuthorities(Set.of("ROLE_ADMIN")),
                                new UserData().setUserId(8L).setUserName("bob").setAuthorities(Set.of("ROLE_USER"))))));
        Mockito
            .when(sessionQueryClient.findSessions(Mockito.any(FindSessionsRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindSessionsReply()
                        .setSuccess(true)
                        .setSessions(
                            List.of(
                                new SessionData()
                                    .setSessionId(11L)
                                    .setUserId(7L)
                                    .setLoginMethod(SessionLoginMethod.OKTA)
                                    .setCreated(NOW.minus(Duration.ofHours(1)))
                                    .setLastActivity(NOW)
                                    .setExpires(NOW.plus(Duration.ofHours(8)))))));
        Mockito
            .when(feedbackQueryClient.findAllFeedback(Mockito.any(FindAllFeedbackRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllFeedbackReply()
                        .setSuccess(true)
                        .setFeedback(List.of(new FeedbackData().setType(FeedbackType.DEFECT).setStatus(FeedbackStatus.NEW).setCreated(NOW)))));
        Mockito
            .when(surveyQueryClient.findAllSurveyResponses(Mockito.any(FindAllSurveyResponsesRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindAllSurveyResponsesReply().setSuccess(false).setFailureMessage("boom")));
    }

    @Test
    public void
    loadsEveryMetricSectionForTheCaller()
        throws Exception
    {
        HomeOutput output = run(createView(11L,7L));

        Mockito.verify(sessionQueryClient).findSessions(Mockito.argThat(r -> r.isIncludeClosed()));
        assertEquals("alice",output.getUserName());
        assertEquals(7L,output.getUserId());
        assertEquals("Home.Current",output.getSource());
        assertEquals("HomeView",output.getDestination());
        assertEquals(NOW.toString(),output.getGenerated());
        assertEquals(2,output.getUsers().getTotal());
        assertEquals(1,output.getSessions().getOpen());
        assertEquals(1,output.getFeedback().getTotal());
        assertNull(output.getSurvey());
        assertEquals(List.of("Unable to read survey responses: boom"),output.getFailureMessages());
    }

    @Test
    public void
    reportsADownstreamExceptionWithoutLosingTheOtherSections()
        throws Exception
    {
        Mockito
            .when(feedbackQueryClient.findAllFeedback(Mockito.any(FindAllFeedbackRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));

        HomeOutput output = run(createView(11L,7L));

        assertNull(output.getFeedback());
        assertNotNull(output.getUsers());
        assertNotNull(output.getSessions());
        assertTrue(output.getFailureMessages().stream().anyMatch(m -> m.startsWith("Unable to read feedback: ")));
    }

    @Test
    public void
    reportsAMissingClientAsAFailure()
        throws Exception
    {
        clients.setSurveyQueryClient(() -> { throw new IllegalStateException("survey not configured"); });

        HomeOutput output = run(createView(11L,7L));

        assertTrue(output.getFailureMessages().contains("Unable to read survey responses: survey not configured"));
    }

    @Test
    public void
    refusesWithoutAVerifiedCaller()
        throws Exception
    {
        HomeOutput output = run(createView(null,null));

        assertEquals(List.of(InitializeHomeView.NOT_AUTHENTICATED),output.getFailureMessages());
        assertNull(output.getUserId());
        Mockito.verifyNoInteractions(userQueryClient,sessionQueryClient,feedbackQueryClient,surveyQueryClient);
    }

    private InitializeHomeView
    createView(Long sessionId,Long caller)
    {
        return
            new InitializeHomeView(
                new HomeMetricsLoader(
                    clients,
                    userQueryClient,
                    sessionQueryClient,
                    new HomeMetricsCalculator(new GraniteApplicationRolesManagerSupplier().get(),Clock.fixed(NOW,ZoneOffset.UTC))),
                SessionTestSupport.context("d",sessionId,caller));
    }

    private HomeOutput
    run(InitializeHomeView view)
        throws Exception
    {
        GetHomeOutputRequest request = new GetHomeOutputRequest().setSource("Home.Current").setDestination("HomeView");

        return view.apply(request).toCompletableFuture().get();
    }
}

//////////////////////////////////////////////////////////////////////////////
