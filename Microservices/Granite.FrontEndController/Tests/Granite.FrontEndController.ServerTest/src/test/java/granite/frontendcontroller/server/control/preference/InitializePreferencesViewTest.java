//////////////////////////////////////////////////////////////////////////////
// InitializePreferencesViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.preference;

import granite.frontendcontroller.server.application.PreferenceDefinition;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputRequest;
import granite.frontendcontroller.service.requestreply.PreferencesOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class InitializePreferencesViewTest
{
    private static final List<PreferenceDefinition> DEFINITIONS =
        List.of(
            PreferenceDefinition.enumeration("granite.theme",List.of("light","dark"),"light"),
            PreferenceDefinition.bool("compact",false));

    private IUserQueryServiceClient queryClient;

    @BeforeEach
    public void
    setUp()
    {
        queryClient = Mockito.mock(IUserQueryServiceClient.class);
        storedValues(Map.of());
    }

    private void
    storedValues(Map<String,Object> values)
    {
        Mockito
            .when(queryClient.findUsersByKey(Mockito.any(FindUsersByKeyRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindUsersByKeyReply()
                        .setSuccess(true)
                        .setFoundUsers(List.of(new UserData().setUserId(7L).setUserName("amy").setValues(values)))));
    }

    private PreferencesOutput
    run(Long caller,Long requestUserId)
        throws Exception
    {
        return
            new InitializePreferencesView(queryClient,SessionTestSupport.context("d",caller == null ? null : 11L,caller),DEFINITIONS)
                .apply(
                    new GetPreferencesOutputRequest()
                        .setUserId(requestUserId)
                        .setSource("Preferences.Current")
                        .setDestination("PreferencesView"))
                .toCompletableFuture()
                .get();
    }

    @Test
    public void
    getReturnsDefaultsWhenUnset()
        throws Exception
    {
        PreferencesOutput output = run(7L,null);

        assertEquals("light",output.getValues().get("granite.theme"));
        assertEquals(false,output.getValues().get("compact"));
        assertEquals(2,output.getDefinitions().size());
        assertEquals("granite.theme",output.getDefinitions().get(0).getKey());
        assertEquals("ENUM",output.getDefinitions().get(0).getType());
        assertEquals(List.of("light","dark"),output.getDefinitions().get(0).getAllowedValues());
        assertEquals("light",output.getDefinitions().get(0).getDefaultValue());
        assertTrue(output.getFailureMessages().isEmpty());
        assertEquals("Preferences.Current",output.getSource());
        assertEquals("PreferencesView",output.getDestination());
    }

    @Test
    public void
    getReturnsStoredValues()
        throws Exception
    {
        storedValues(Map.of("preferences.granite.theme","dark","preferences.compact",true,"other","x"));

        PreferencesOutput output = run(7L,null);

        assertEquals("dark",output.getValues().get("granite.theme"));
        assertEquals(true,output.getValues().get("compact"));
        assertEquals(2,output.getValues().size());
    }

    @Test
    public void
    getReportsInvalidStoredValueAsDefault()
        throws Exception
    {
        storedValues(Map.of("preferences.granite.theme","purple","preferences.compact","yes"));

        PreferencesOutput output = run(7L,null);

        assertEquals("light",output.getValues().get("granite.theme"));
        assertEquals(false,output.getValues().get("compact"));
    }

    @Test
    public void
    getNormalisesStoredBooleanText()
        throws Exception
    {
        storedValues(Map.of("preferences.compact","TRUE"));

        assertEquals(true,run(7L,null).getValues().get("compact"));
    }

    @Test
    public void
    ignoresRequestUserId()
        throws Exception
    {
        PreferencesOutput output = run(7L,99L);

        ArgumentCaptor<FindUsersByKeyRequest> captor = ArgumentCaptor.forClass(FindUsersByKeyRequest.class);

        Mockito.verify(queryClient).findUsersByKey(captor.capture());
        assertEquals("7",captor.getValue().getKey().getValue());
        assertEquals(7L,output.getUserId());
    }

    @Test
    public void
    unauthenticatedFails()
        throws Exception
    {
        PreferencesOutput output = run(null,7L);

        assertEquals(List.of("not authenticated"),output.getFailureMessages());
        assertTrue(output.getValues().isEmpty());
        Mockito.verifyNoInteractions(queryClient);
    }
}

//////////////////////////////////////////////////////////////////////////////
