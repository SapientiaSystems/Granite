//////////////////////////////////////////////////////////////////////////////
// ChangePasswordViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.ChangePasswordReply;
import granite.session.service.requestreply.ChangePasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import strata.foundation.core.mapper.ObjectMapperSupplier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("CommitStage")
public
class ChangePasswordViewTest
{
    private static final String CURRENT = "current-secret-value";
    private static final String NEXT    = "next-secret-value";

    private ISessionCommandServiceClient commandClient;

    @BeforeEach
    public void
    setUp()
    {
        commandClient = Mockito.mock(ISessionCommandServiceClient.class);
    }

    @Test
    public void
    noCallerIsRefusedWithoutCallingSession()
        throws Exception
    {
        SessionOutput output = run(SessionTestSupport.context("d",null,null),true);

        assertEquals(List.of("not authenticated"),output.getFailureMessages());
        Mockito.verifyNoInteractions(commandClient);
    }

    @Test
    public void
    disabledPasswordLoginIsRefusedWithoutCallingSession()
        throws Exception
    {
        SessionOutput output = run(SessionTestSupport.context("d",11L,7L),false);

        assertEquals(List.of("password change is not available"),output.getFailureMessages());
        Mockito.verifyNoInteractions(commandClient);
    }

    @Test
    public void
    successIsRelayedWithTheCallerSession()
        throws Exception
    {
        Mockito
            .when(commandClient.changePassword(Mockito.any(ChangePasswordRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new ChangePasswordReply().setSuccess(true)));

        SessionOutput output = run(SessionTestSupport.context("d",11L,7L),true);

        ArgumentCaptor<ChangePasswordRequest> captor = ArgumentCaptor.forClass(ChangePasswordRequest.class);

        Mockito.verify(commandClient).changePassword(captor.capture());
        assertEquals(11L,captor.getValue().getSessionId());
        assertEquals(CURRENT,captor.getValue().getCurrentPassword());
        assertEquals(NEXT,captor.getValue().getNewPassword());
        assertEquals("CHANGED",output.getStatus());
        assertEquals("Session.Current",output.getSource());
        assertEquals("ChangePasswordView",output.getDestination());
        assertEquals(0,output.getFailureMessages().size());
        assertFalse(new ObjectMapperSupplier().get().writeValueAsString(output).contains(NEXT));
    }

    @Test
    public void
    sessionFailureMessageIsRelayed()
        throws Exception
    {
        Mockito
            .when(commandClient.changePassword(Mockito.any(ChangePasswordRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new ChangePasswordReply().setSuccess(false).setFailureMessage("invalid credentials")));

        SessionOutput output = run(SessionTestSupport.context("d",11L,7L),true);

        assertEquals("FAILED",output.getStatus());
        assertEquals(List.of("invalid credentials"),output.getFailureMessages());
    }

    @Test
    public void
    exceptionBecomesAGenericFailure()
        throws Exception
    {
        Mockito
            .when(commandClient.changePassword(Mockito.any(ChangePasswordRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException(CURRENT)));

        SessionOutput output = run(SessionTestSupport.context("d",11L,7L),true);

        assertEquals(List.of("Unable to change password"),output.getFailureMessages());
    }

    @Test
    public void
    inputToStringOmitsThePasswords()
    {
        String text = new SessionInput().setCurrentPassword(CURRENT).setNewPassword(NEXT).setPassword("login-secret").toString();

        assertFalse(text.contains(CURRENT));
        assertFalse(text.contains(NEXT));
        assertFalse(text.contains("login-secret"));
    }

    private SessionOutput
    run(SessionRequestContext context,boolean passwordEnabled)
        throws Exception
    {
        return
            new ChangePasswordView(commandClient,context,new SessionLoginSettings(passwordEnabled,!passwordEnabled))
                .apply(
                    new SessionInput()
                        .setCurrentPassword(CURRENT)
                        .setNewPassword(NEXT)
                        .setSource("Session.Current")
                        .setCommand("ChangePassword")
                        .setDestination("ChangePasswordView"))
                .toCompletableFuture()
                .get();
    }
}

//////////////////////////////////////////////////////////////////////////////
