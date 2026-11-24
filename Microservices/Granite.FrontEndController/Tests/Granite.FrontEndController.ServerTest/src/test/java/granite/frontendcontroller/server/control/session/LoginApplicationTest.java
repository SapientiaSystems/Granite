//////////////////////////////////////////////////////////////////////////////
// LoginApplicationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
public
class LoginApplicationTest
{
    private ISessionCommandServiceClient commandClient;

    @BeforeEach
    public void
    setUp()
    {
        commandClient = Mockito.mock(ISessionCommandServiceClient.class);
        Mockito
            .when(commandClient.login(Mockito.any(LoginRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new LoginReply().setSuccess(false)));
        Mockito
            .when(commandClient.startOktaLogin(Mockito.any(StartOktaLoginRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new StartOktaLoginReply().setSuccess(false)));
    }

    private String
    passwordLogin(String application)
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        new LoginView(commandClient,context,new SessionLoginSettings(true,true),SessionTestSupport.COOKIES)
            .apply(SessionTestSupport.login("alice","pw").setApplication(application))
            .toCompletableFuture()
            .get();

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);

        Mockito.verify(commandClient).login(captor.capture());
        return captor.getValue().getApplication();
    }

    private String
    oktaStart(String application)
        throws Exception
    {
        SessionRequestContext context = SessionTestSupport.context("device-1",null,null);

        new StartOktaLoginView(commandClient,context,new SessionLoginSettings(true,true),SessionTestSupport.COOKIES)
            .apply(
                new SessionInput()
                    .setReturnTo("/")
                    .setApplication(application)
                    .setSource("Session.New")
                    .setCommand("StartOktaLogin")
                    .setDestination("LoginView"))
            .toCompletableFuture()
            .get();

        ArgumentCaptor<StartOktaLoginRequest> captor = ArgumentCaptor.forClass(StartOktaLoginRequest.class);

        Mockito.verify(commandClient).startOktaLogin(captor.capture());
        return captor.getValue().getApplication();
    }

    @Test
    public void
    loginForwardsApplication()
        throws Exception
    {
        assertEquals("client",passwordLogin("client"));
    }

    @Test
    public void
    loginWithoutApplicationUsesGranite()
        throws Exception
    {
        assertEquals("granite",passwordLogin(null));
    }

    @Test
    public void
    loginWithBlankApplicationUsesGranite()
        throws Exception
    {
        assertEquals("granite",passwordLogin("  "));
    }

    @Test
    public void
    oktaStartForwardsApplication()
        throws Exception
    {
        assertEquals("client",oktaStart("client"));
    }

    @Test
    public void
    oktaStartWithoutApplicationUsesGranite()
        throws Exception
    {
        assertEquals("granite",oktaStart(null));
    }
}

//////////////////////////////////////////////////////////////////////////////
