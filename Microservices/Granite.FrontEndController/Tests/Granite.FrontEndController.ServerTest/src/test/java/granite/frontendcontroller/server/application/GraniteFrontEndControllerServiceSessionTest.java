//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerServiceSessionTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleSessionInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionInputRequest;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteFrontEndControllerServiceSessionTest
{
    private GraniteFrontEndControllerService service;

    @BeforeEach
    public void
    setUp()
    {
        service =
            new GraniteFrontEndControllerService(
                new ModelMapper(),
                Runnable::run,
                Mockito.mock(IUserQueryServiceClient.class),
                Mockito.mock(IUserCommandServiceClient.class),
                Mockito.mock(ISessionCommandServiceClient.class),
                Mockito.mock(ISessionQueryServiceClient.class),
                SessionTestSupport.context(null,null,null),
                new SessionLoginSettings(true,false));
    }

    private HandleSessionInputReply
    handle(String command)
        throws Exception
    {
        return
            service
                .handleSessionInput(
                    new HandleSessionInputRequest()
                        .setInput(
                            new SessionInput()
                                .setReturnTo("/")
                                .setCode("c")
                                .setState("s")
                                .setUserName("amy")
                                .setPassword("pw")
                                .setSource("Session.New")
                                .setCommand(command)
                                .setDestination("LoginView")))
                .toCompletableFuture()
                .get();
    }

    @Test
    public void
    loginMethodsAreServedForTheNewSessionKey()
        throws Exception
    {
        GetSessionOutputReply reply =
            service
                .getSessionOutput(new GetSessionOutputRequest().setSource("Session.New").setDestination("LoginView"))
                .toCompletableFuture()
                .get();

        assertTrue(reply.isSuccess());
        assertTrue(reply.getOutput().isPasswordEnabled());
        assertFalse(reply.getOutput().isOktaEnabled());
        assertEquals("Session.New",reply.getOutput().getSource());
    }

    @Test
    public void
    currentSessionKeyIsDispatchedToTheStatusView()
        throws Exception
    {
        GetSessionOutputReply reply =
            service
                .getSessionOutput(new GetSessionOutputRequest().setSource("Session.Current").setDestination("SessionView"))
                .toCompletableFuture()
                .get();

        assertTrue(reply.isSuccess());
        assertEquals("UNAUTHENTICATED",reply.getOutput().getStatus());
    }

    @Test
    public void
    loginKeysAreDispatchedToTheirActions()
        throws Exception
    {
        assertEquals("UNAUTHENTICATED",handle("Login").getOutput().getStatus());

        HandleSessionInputReply start = handle("StartOktaLogin");

        assertEquals("UNAUTHENTICATED",start.getOutput().getStatus());
        assertNull(start.getOutput().getRedirectUrl());
        assertEquals("UNAUTHENTICATED",handle("CompleteOktaLogin").getOutput().getStatus());
    }

    @Test
    public void
    logoutKeyIsDispatchedToTheLogoutView()
        throws Exception
    {
        HandleSessionInputReply reply =
            service
                .handleSessionInput(new HandleSessionInputRequest().setInput(SessionTestSupport.logout()))
                .toCompletableFuture()
                .get();

        assertTrue(reply.isSuccess());
        assertTrue(reply.getOutput().isLoggedOut());
    }

    @Test
    public void
    unknownKeysFailTheReply()
        throws Exception
    {
        HandleSessionInputReply reply = handle("Unknown");

        assertFalse(reply.isSuccess());
        assertEquals("java.lang.IllegalArgumentException",reply.getException().getCause().getExceptionType());
        assertEquals("No action found for key: Session.New->Unknown->LoginView",reply.getException().getCause().getExceptionMessage());

        GetSessionOutputReply output =
            service
                .getSessionOutput(new GetSessionOutputRequest().setSource("Session.Other").setDestination("LoginView"))
                .toCompletableFuture()
                .get();

        assertFalse(output.isSuccess());
    }
}

//////////////////////////////////////////////////////////////////////////////
