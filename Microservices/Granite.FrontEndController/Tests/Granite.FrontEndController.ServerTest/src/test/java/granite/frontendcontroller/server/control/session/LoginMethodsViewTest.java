//////////////////////////////////////////////////////////////////////////////
// LoginMethodsViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.service.requestreply.LoginMethodsOutput;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import strata.foundation.core.configuration.IConfiguration;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class LoginMethodsViewTest
{
    private LoginMethodsOutput
    run(SessionLoginSettings settings)
        throws Exception
    {
        return
            assertInstanceOf(
                LoginMethodsOutput.class,
                new LoginMethodsView(settings)
                    .apply(new GetOutputRequest().setUserId(0L).setSource("Session.New").setDestination("LoginView"))
                    .toCompletableFuture()
                    .get());
    }

    @Test
    public void
    reportsBothSwitchesAndCopiesTheKey()
        throws Exception
    {
        LoginMethodsOutput output = run(new SessionLoginSettings(true,true));

        assertTrue(output.isPasswordEnabled());
        assertTrue(output.isOktaEnabled());
        assertEquals("Session.New",output.getSource());
        assertEquals("LoginView",output.getDestination());
    }

    @Test
    public void
    disabledPasswordLoginIsMissingFromLoginMethods()
        throws Exception
    {
        LoginMethodsOutput output = run(new SessionLoginSettings(false,false));

        assertFalse(output.isPasswordEnabled());
        assertFalse(output.isOktaEnabled());
    }

    @Test
    public void
    settingsDefaultToPasswordOnOktaOff()
    {
        IConfiguration configuration = Mockito.mock(IConfiguration.class);

        Mockito.when(configuration.getProperty(Mockito.anyString(),Mockito.anyString())).thenAnswer(i -> i.getArgument(1));

        SessionLoginSettings defaults = SessionLoginSettings.fromConfiguration(configuration);

        assertTrue(defaults.isPasswordEnabled());
        assertFalse(defaults.isOktaEnabled());

        Mockito.when(configuration.getProperty("granite.session.login.password.enabled","true")).thenReturn("false");
        Mockito.when(configuration.getProperty("granite.session.login.okta.enabled","false")).thenReturn("true");

        SessionLoginSettings changed = SessionLoginSettings.fromConfiguration(configuration);

        assertFalse(changed.isPasswordEnabled());
        assertTrue(changed.isOktaEnabled());
    }
}

//////////////////////////////////////////////////////////////////////////////
