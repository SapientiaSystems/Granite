//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerContextTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CommitStage")
public
class GraniteFrontEndControllerContextTest
{
    private GraniteFrontEndControllerContext
    contextWith(List<PreferenceDefinition> definitions)
    {
        return
            new GraniteFrontEndControllerContext(
                new ModelMapper(),
                Runnable::run,
                Mockito.mock(IUserQueryServiceClient.class),
                Mockito.mock(IUserCommandServiceClient.class),
                Mockito.mock(ISessionCommandServiceClient.class),
                Mockito.mock(ISessionQueryServiceClient.class),
                SessionTestSupport.context(null,null,null),
                new SessionLoginSettings(true,false),
                new GraniteApplicationRolesManagerSupplier().get(),
                definitions);
    }

    @Test
    public void
    duplicatePreferenceKeysFailFast()
    {
        IllegalArgumentException failure =
            assertThrows(
                IllegalArgumentException.class,
                () ->
                    contextWith(
                        List.of(
                            PreferenceDefinition.enumeration("granite.theme",List.of("light","dark"),"light"),
                            PreferenceDefinition.bool("granite.theme",false))));

        assertEquals("duplicate preference key: granite.theme",failure.getMessage());
    }

    @Test
    public void
    distinctPreferenceKeysAreAccepted()
    {
        assertEquals(
            2,
            contextWith(List.of(PreferenceDefinition.bool("a",false),PreferenceDefinition.bool("b",false))).getPreferenceDefinitions().size());
    }
}

//////////////////////////////////////////////////////////////////////////////
