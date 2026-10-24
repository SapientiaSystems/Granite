//////////////////////////////////////////////////////////////////////////////
// SessionRequestProcessorTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.service.requestreply.CompleteOktaLoginInput;
import granite.frontendcontroller.service.requestreply.LoginInput;
import granite.frontendcontroller.service.requestreply.LoginMethodsOutput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.frontendcontroller.service.requestreply.StartOktaLoginInput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionRequestProcessorTest
{
    private SessionRequestProcessor processor;

    @BeforeEach
    public void
    setUp()
    {
        processor =
            new SessionRequestProcessor(
                Mockito.mock(ISessionCommandServiceClient.class),
                Mockito.mock(ISessionQueryServiceClient.class),
                Mockito.mock(IUserQueryServiceClient.class),
                SessionTestSupport.context(null,null,null),
                new SessionLoginSettings(true,false));
    }

    @Test
    public void
    routesLoginMethodsAndLogin()
        throws Exception
    {
        assertInstanceOf(
            LoginMethodsOutput.class,
            processor
                .process(new GetOutputRequest().setUserId(0L).setSource("Session.New").setDestination("LoginView"))
                .toCompletableFuture()
                .get());
        assertInstanceOf(
            SessionOutput.class,
            processor.process(SessionTestSupport.login("amy","pw")).toCompletableFuture().get());
    }

    @Test
    public void
    oktaKeysRouteToTheOktaActions()
        throws Exception
    {
        HandleInputRequest start =
            new HandleInputRequest()
                .setInput(new StartOktaLoginInput().setReturnTo("/").setSource("Session.New").setCommand("StartOktaLogin").setDestination("LoginView"));
        HandleInputRequest complete =
            new HandleInputRequest()
                .setInput(
                    new CompleteOktaLoginInput()
                        .setCode("c")
                        .setState("s")
                        .setSource("Session.New")
                        .setCommand("CompleteOktaLogin")
                        .setDestination("LoginView"));

        assertEquals("UNAUTHENTICATED",assertInstanceOf(SessionOutput.class,processor.process(start).toCompletableFuture().get()).getStatus());
        assertEquals("UNAUTHENTICATED",assertInstanceOf(SessionOutput.class,processor.process(complete).toCompletableFuture().get()).getStatus());
    }

    @Test
    public void
    unknownKeysHaveNoAction()
    {
        HandleInputRequest unknown =
            new HandleInputRequest()
                .setInput(new LoginInput().setSource("Session.New").setCommand("Unknown").setDestination("LoginView"));

        ExecutionException e =
            assertThrows(ExecutionException.class,() -> processor.process(unknown).toCompletableFuture().get());

        assertTrue(e.getCause().getMessage().contains("No action found for key"));
    }
}

//////////////////////////////////////////////////////////////////////////////
