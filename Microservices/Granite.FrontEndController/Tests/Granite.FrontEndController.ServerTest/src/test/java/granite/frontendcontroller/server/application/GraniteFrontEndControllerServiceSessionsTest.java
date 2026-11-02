//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerServiceSessionsTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.GetSessionsOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionsOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleSessionsInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionsInputRequest;
import granite.frontendcontroller.service.requestreply.SessionsInput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.CloseSessionsReply;
import granite.session.service.requestreply.CloseSessionsRequest;
import granite.session.service.requestreply.FindSessionsReply;
import granite.session.service.requestreply.FindSessionsRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteFrontEndControllerServiceSessionsTest
{
    private ISessionCommandServiceClient sessionCommandClient;
    private ISessionQueryServiceClient   sessionQueryClient;
    private IUserQueryServiceClient      userQueryClient;

    @BeforeEach
    public void
    setUp()
    {
        sessionCommandClient = Mockito.mock(ISessionCommandServiceClient.class);
        sessionQueryClient   = Mockito.mock(ISessionQueryServiceClient.class);
        userQueryClient      = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(sessionQueryClient.findSessions(Mockito.any(FindSessionsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindSessionsReply().setSuccess(true).setSessions(List.of())));
        Mockito
            .when(sessionCommandClient.closeSessions(Mockito.any(CloseSessionsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new CloseSessionsReply().setSuccess(true)));
    }

    @Test
    public void
    dispatchesLoadForOpenAndAllSources()
        throws Exception
    {
        GraniteFrontEndControllerService service = createService(SessionTestSupport.context("d",11L,1L));

        for (String source : List.of("Session.Open","Session.All"))
        {
            GetSessionsOutputReply load =
                service
                    .getSessionsOutput(new GetSessionsOutputRequest().setSource(source).setDestination("SessionsView"))
                    .toCompletableFuture()
                    .get();

            assertTrue(load.isSuccess(),source);
            assertEquals(source,load.getOutput().getSource());
            assertEquals(11L,load.getOutput().getCurrentSessionId());
        }

        Mockito.verify(sessionQueryClient,Mockito.times(2)).findSessions(Mockito.any(FindSessionsRequest.class));
    }

    @Test
    public void
    dispatchesClose()
        throws Exception
    {
        HandleSessionsInputReply close =
            createService(SessionTestSupport.context("d",11L,1L))
                .handleSessionsInput(
                    new HandleSessionsInputRequest()
                        .setInput(
                            new SessionsInput()
                                .setSessionIdsToClose(List.of(12L))
                                .setSource("Session.Open")
                                .setCommand("Close")
                                .setDestination("SessionsView")))
                .toCompletableFuture()
                .get();

        assertTrue(close.isSuccess());
        Mockito.verify(sessionCommandClient).closeSessions(Mockito.any(CloseSessionsRequest.class));
    }

    @Test
    public void
    unknownKeyFails()
        throws Exception
    {
        GetSessionsOutputReply reply =
            createService(SessionTestSupport.context("d",11L,1L))
                .getSessionsOutput(new GetSessionsOutputRequest().setSource("Session.Mine").setDestination("SessionsView"))
                .toCompletableFuture()
                .get();

        assertFalse(reply.isSuccess());
        Mockito.verifyNoInteractions(sessionQueryClient);
    }

    @Test
    public void
    refusesWithoutCaller()
        throws Exception
    {
        GetSessionsOutputReply reply =
            createService(SessionTestSupport.context("d",null,null))
                .getSessionsOutput(new GetSessionsOutputRequest().setSource("Session.Open").setDestination("SessionsView"))
                .toCompletableFuture()
                .get();

        assertEquals(List.of("not authenticated"),reply.getOutput().getFailureMessages());
        Mockito.verifyNoInteractions(sessionQueryClient,sessionCommandClient,userQueryClient);
    }

    private GraniteFrontEndControllerService
    createService(SessionRequestContext requestContext)
    {
        return
            new GraniteFrontEndControllerService(
                new GraniteFrontEndControllerContext(
                    new ModelMapper(),
                    Runnable::run,
                    userQueryClient,
                    Mockito.mock(IUserCommandServiceClient.class),
                    sessionCommandClient,
                    sessionQueryClient,
                    requestContext,
                    new SessionLoginSettings(true,false),
                    SessionTestSupport.COOKIES,
                    new GraniteApplicationRolesManagerSupplier().get(),
                    List.of()));
    }
}

//////////////////////////////////////////////////////////////////////////////
