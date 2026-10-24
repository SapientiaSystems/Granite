//////////////////////////////////////////////////////////////////////////////
// SessionRequestProcessorTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.service.requestreply.LoginInput;
import granite.frontendcontroller.service.requestreply.LoginMethodsOutput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
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
    oktaKeysHaveNoActionYet()
    {
        HandleInputRequest okta =
            new HandleInputRequest()
                .setInput(new LoginInput().setSource("Session.New").setCommand("StartOktaLogin").setDestination("LoginView"));

        ExecutionException e =
            assertThrows(ExecutionException.class,() -> processor.process(okta).toCompletableFuture().get());

        assertTrue(e.getCause().getMessage().contains("No action found for key"));
    }
}

//////////////////////////////////////////////////////////////////////////////
