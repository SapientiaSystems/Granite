//////////////////////////////////////////////////////////////////////////////
// CloseSessionsViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.sessions;

import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.SessionsInput;
import granite.frontendcontroller.service.requestreply.SessionsOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.CloseSessionsReply;
import granite.session.service.requestreply.CloseSessionsRequest;
import granite.session.service.requestreply.FindSessionsRequest;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeySetRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import static granite.frontendcontroller.server.control.sessions.SessionsViewTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class CloseSessionsViewTest
{
    private ISessionCommandServiceClient commandClient;
    private ISessionQueryServiceClient   sessionQueryClient;
    private IUserQueryServiceClient      userQueryClient;

    @BeforeEach
    public void
    setUp()
    {
        commandClient      = Mockito.mock(ISessionCommandServiceClient.class);
        sessionQueryClient = Mockito.mock(ISessionQueryServiceClient.class);
        userQueryClient    = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(commandClient.closeSessions(Mockito.any(CloseSessionsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new CloseSessionsReply().setSuccess(true)));
        Mockito
            .when(sessionQueryClient.findSessions(Mockito.any(FindSessionsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(sessions(session(11L,7L))));
        Mockito
            .when(userQueryClient.findUsersByKeySet(Mockito.any(FindUsersByKeySetRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(users(user(7L,"admin"))));
    }

    @Test
    public void
    refusesTheCallersOwnSessionAndClosesTheRest()
        throws Exception
    {
        SessionsOutput output = run(createView(11L,7L),input("Session.Open",11L,12L,13L));

        Mockito.verify(commandClient).closeSessions(Mockito.argThat(r -> r.getSessionIds().equals(Set.of(12L,13L))));
        assertEquals(List.of("11: cannot end your own session"),output.getFailureMessages());
        assertEquals(11L,output.getCurrentSessionId());
        assertEquals(1,output.getSessions().size());
        assertEquals(Optional.of("Close"),output.getCommand());
    }

    @Test
    public void
    usesTheVerifiedSessionIdNotThePayloadUserId()
        throws Exception
    {
        SessionsInput input = input("Session.Open",11L).setUserId(99L);

        SessionsOutput output = run(createView(11L,7L),input);

        Mockito.verifyNoInteractions(commandClient);
        assertEquals(7L,output.getUserId());
        assertEquals(1,output.getFailureMessages().size());
    }

    @Test
    public void
    reportsSessionsThatWereNotEnded()
        throws Exception
    {
        Mockito
            .when(commandClient.closeSessions(Mockito.any(CloseSessionsRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new CloseSessionsReply()
                        .setSuccess(false)
                        .setFailureMessage("Unable to close 1 session(s)")
                        .setFailedSessionIds(new TreeSet<>(Set.of(13L)))));

        SessionsOutput output = run(createView(11L,7L),input("Session.All",12L,13L));

        assertEquals(List.of("13: not ended"),output.getFailureMessages());
        Mockito.verify(sessionQueryClient).findSessions(Mockito.argThat(r -> r.isIncludeClosed()));
    }

    @Test
    public void
    reportsBatchFailureWithoutIds()
        throws Exception
    {
        Mockito
            .when(commandClient.closeSessions(Mockito.any(CloseSessionsRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new CloseSessionsReply().setSuccess(false).setFailureMessage("Unable to close sessions")));

        SessionsOutput output = run(createView(11L,7L),input("Session.Open",12L));

        assertEquals(List.of("Unable to close sessions"),output.getFailureMessages());
    }

    @Test
    public void
    refusesWithoutVerifiedCaller()
        throws Exception
    {
        SessionsOutput output = run(createView(null,null),input("Session.Open",12L));

        assertTrue(output.getSessions().isEmpty());
        assertEquals(List.of(UnauthenticatedSessionsOutput.MESSAGE),output.getFailureMessages());
        Mockito.verifyNoInteractions(commandClient,sessionQueryClient,userQueryClient);
    }

    private CloseSessionsView
    createView(Long sessionId,Long caller)
    {
        return
            new CloseSessionsView(
                commandClient,
                new SessionsLoader(sessionQueryClient,userQueryClient),
                SessionTestSupport.context("d",sessionId,caller));
    }

    private SessionsInput
    input(String source,Long... ids)
    {
        return
            new SessionsInput()
                .setSessionIdsToClose(List.of(ids))
                .setSource(source)
                .setCommand("Close")
                .setDestination("SessionsView");
    }

    private SessionsOutput
    run(CloseSessionsView view,SessionsInput input)
        throws Exception
    {
        return view.apply(input).toCompletableFuture().get();
    }
}

//////////////////////////////////////////////////////////////////////////////
