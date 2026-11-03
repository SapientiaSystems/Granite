//////////////////////////////////////////////////////////////////////////////
// InitializeSessionsViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.sessions;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.GetSessionsOutputRequest;
import granite.frontendcontroller.service.requestreply.SessionRowData;
import granite.frontendcontroller.service.requestreply.SessionsOutput;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.FindSessionsReply;
import granite.session.service.requestreply.FindSessionsRequest;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeySetReply;
import granite.user.service.requestreply.FindUsersByKeySetRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static granite.frontendcontroller.server.control.sessions.SessionsViewTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class InitializeSessionsViewTest
{
    private ISessionQueryServiceClient sessionQueryClient;
    private IUserQueryServiceClient    userQueryClient;

    @BeforeEach
    public void
    setUp()
    {
        sessionQueryClient = Mockito.mock(ISessionQueryServiceClient.class);
        userQueryClient    = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(sessionQueryClient.findSessions(Mockito.any(FindSessionsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(sessions(session(11L,7L),session(12L,8L))));
        Mockito
            .when(userQueryClient.findUsersByKeySet(Mockito.any(FindUsersByKeySetRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(users(user(7L,"admin"),user(8L,"alice"))));
    }

    @Test
    public void
    listsOpenSessionsWithUserNamesAndCurrentSession()
        throws Exception
    {
        SessionsOutput output = run(createView(11L,7L),"Session.Open");

        Mockito.verify(sessionQueryClient).findSessions(Mockito.argThat(r -> !r.isIncludeClosed()));
        Mockito.verify(userQueryClient).findUsersByKeySet(Mockito.argThat(r -> r.getKeys().getValuesAsLongs().equals(Set.of(7L,8L))));
        assertTrue(output.getFailureMessages().isEmpty());
        assertEquals(11L,output.getCurrentSessionId());
        assertEquals(7L,output.getUserId());
        assertEquals("Session.Open",output.getSource());
        assertEquals("SessionsView",output.getDestination());
        assertEquals(List.of("ROLE_ADMIN","ROLE_AUDITOR","ROLE_USER"),output.getRoles().stream().map(r -> r.getName()).toList());
        assertEquals(2,output.getSessions().size());

        SessionRowData row = output.getSessions().get(1);

        assertEquals(12L,row.getSessionId());
        assertEquals("alice",row.getUserName());
        assertEquals(List.of("ROLE_ADMIN","ROLE_USER"),row.getAuthorities());
        assertEquals("PASSWORD",row.getLoginMethod());
        assertEquals(NOW.minusSeconds(60).toString(),row.getLastActivity());
        assertNull(row.getClosedReason());
        assertEquals(3,row.getValues().get("cart"));
    }

    @Test
    public void
    includesClosedSessionsForTheAllSource()
        throws Exception
    {
        Mockito
            .when(sessionQueryClient.findSessions(Mockito.any(FindSessionsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(sessions(session(11L,7L),closed(13L,8L))));

        SessionsOutput output = run(createView(11L,7L),"Session.All");

        Mockito.verify(sessionQueryClient).findSessions(Mockito.argThat(r -> r.isIncludeClosed()));
        assertEquals("ADMIN",output.getSessions().get(1).getClosedReason());
    }

    @Test
    public void
    reportsRefreshFailureWithoutRows()
        throws Exception
    {
        Mockito
            .when(sessionQueryClient.findSessions(Mockito.any(FindSessionsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindSessionsReply().setSuccess(false).setFailureMessage("boom")));

        SessionsOutput output = run(createView(11L,7L),"Session.Open");

        assertTrue(output.getSessions().isEmpty());
        assertEquals(List.of("Unable to refresh session list: boom"),output.getFailureMessages());
        Mockito.verifyNoInteractions(userQueryClient);
    }

    @Test
    public void
    keepsRowsWhenUserNamesCannotBeResolved()
        throws Exception
    {
        Mockito
            .when(userQueryClient.findUsersByKeySet(Mockito.any(FindUsersByKeySetRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindUsersByKeySetReply().setSuccess(false).setFailureMessage("down")));

        SessionsOutput output = run(createView(11L,7L),"Session.Open");

        assertEquals(2,output.getSessions().size());
        assertNull(output.getSessions().get(0).getUserName());
        assertEquals(List.of("Unable to resolve user names: down"),output.getFailureMessages());
    }

    @Test
    public void
    refusesWithoutVerifiedCaller()
        throws Exception
    {
        SessionsOutput output = run(createView(null,null),"Session.Open");

        assertTrue(output.getSessions().isEmpty());
        assertEquals(List.of(UnauthenticatedSessionsOutput.MESSAGE),output.getFailureMessages());
        assertNull(output.getUserId());
        Mockito.verifyNoInteractions(sessionQueryClient,userQueryClient);
    }

    private InitializeSessionsView
    createView(Long sessionId,Long caller)
    {
        return
            new InitializeSessionsView(
                new SessionsLoader(sessionQueryClient,userQueryClient),
                SessionTestSupport.context("d",sessionId,caller),
                new GraniteApplicationRolesManagerSupplier().get());
    }

    private SessionsOutput
    run(InitializeSessionsView view,String source)
        throws Exception
    {
        return
            view
                .apply((GetSessionsOutputRequest)new GetSessionsOutputRequest().setSource(source).setDestination("SessionsView"))
                .toCompletableFuture()
                .get();
    }
}

//////////////////////////////////////////////////////////////////////////////
