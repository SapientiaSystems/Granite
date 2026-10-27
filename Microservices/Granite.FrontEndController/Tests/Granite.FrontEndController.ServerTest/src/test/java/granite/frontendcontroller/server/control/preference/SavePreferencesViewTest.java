//////////////////////////////////////////////////////////////////////////////
// SavePreferencesViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.preference;

import granite.frontendcontroller.server.application.PreferenceDefinition;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.PreferencesInput;
import granite.frontendcontroller.service.requestreply.PreferencesOutput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.MergeUserValuesReply;
import granite.user.service.requestreply.MergeUserValuesRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SavePreferencesViewTest
{
    private static final List<PreferenceDefinition> DEFINITIONS =
        List.of(
            PreferenceDefinition.enumeration("granite.theme",List.of("light","dark"),"light"),
            PreferenceDefinition.bool("compact",false));

    private IUserQueryServiceClient   queryClient;
    private IUserCommandServiceClient commandClient;
    private Map<String,Object>        stored;

    @BeforeEach
    public void
    setUp()
    {
        queryClient = Mockito.mock(IUserQueryServiceClient.class);
        commandClient = Mockito.mock(IUserCommandServiceClient.class);
        stored = new HashMap<>();

        Mockito
            .when(queryClient.findUsersByKey(Mockito.any(FindUsersByKeyRequest.class)))
            .thenAnswer(
                i ->
                    CompletableFuture.completedFuture(
                        new FindUsersByKeyReply()
                            .setSuccess(true)
                            .setFoundUsers(List.of(new UserData().setUserId(7L).setValues(new HashMap<>(stored))))));
        Mockito
            .when(commandClient.mergeUserValues(Mockito.any(MergeUserValuesRequest.class)))
            .thenAnswer(
                i ->
                {
                    MergeUserValuesRequest request = i.getArgument(0);

                    for (Map.Entry<String,Object> entry : request.getValues().entrySet())
                    {
                        if (entry.getValue() == null)
                            stored.remove(entry.getKey());
                        else
                            stored.put(entry.getKey(),entry.getValue());
                    }

                    return CompletableFuture.completedFuture(new MergeUserValuesReply().setSuccess(true));
                });
    }

    private PreferencesOutput
    run(Long caller,Long inputUserId,Map<String,Object> values)
        throws Exception
    {
        return
            new SavePreferencesView(
                    queryClient,
                    commandClient,
                    SessionTestSupport.context("d",caller == null ? null : 11L,caller),
                    DEFINITIONS)
                .apply(
                    new PreferencesInput()
                        .setValues(values)
                        .setUserId(inputUserId)
                        .setSource("Preferences.Current")
                        .setCommand("Save")
                        .setDestination("PreferencesView"))
                .toCompletableFuture()
                .get();
    }

    @Test
    public void
    saveStoresUnderPrefix()
        throws Exception
    {
        PreferencesOutput output = run(7L,null,Map.of("granite.theme","dark","compact",true));

        ArgumentCaptor<MergeUserValuesRequest> captor = ArgumentCaptor.forClass(MergeUserValuesRequest.class);

        Mockito.verify(commandClient).mergeUserValues(captor.capture());
        assertEquals(7L,captor.getValue().getUserId());
        assertEquals(Map.of("preferences.granite.theme","dark","preferences.compact",true),captor.getValue().getValues());
        assertEquals("dark",output.getValues().get("granite.theme"));
        assertEquals(true,output.getValues().get("compact"));
        assertTrue(output.getFailureMessages().isEmpty());
        assertEquals(java.util.Optional.of("Save"),output.getCommand());
    }

    @Test
    public void
    saveRejectsUndeclaredKey()
        throws Exception
    {
        PreferencesOutput output = run(7L,null,Map.of("granite.theme","dark","unknown","x"));

        assertEquals(1,output.getFailureMessages().size());
        assertTrue(output.getFailureMessages().get(0).contains("unknown"));
        Mockito.verify(commandClient,Mockito.never()).mergeUserValues(Mockito.any(MergeUserValuesRequest.class));
        assertEquals("light",output.getValues().get("granite.theme"));
    }

    @Test
    public void
    saveRejectsWrongType()
        throws Exception
    {
        PreferencesOutput output = run(7L,null,Map.of("granite.theme","purple"));

        assertEquals(1,output.getFailureMessages().size());
        assertTrue(output.getFailureMessages().get(0).contains("granite.theme"));
        Mockito.verify(commandClient,Mockito.never()).mergeUserValues(Mockito.any(MergeUserValuesRequest.class));
        assertFalse(stored.containsKey("preferences.granite.theme"));
    }

    @Test
    public void
    ignoresRequestUserId()
        throws Exception
    {
        PreferencesOutput output = run(7L,99L,Map.of("compact",true));

        ArgumentCaptor<MergeUserValuesRequest> captor = ArgumentCaptor.forClass(MergeUserValuesRequest.class);

        Mockito.verify(commandClient).mergeUserValues(captor.capture());
        assertEquals(7L,captor.getValue().getUserId());
        assertEquals(7L,output.getUserId());
    }

    @Test
    public void
    unauthenticatedFails()
        throws Exception
    {
        PreferencesOutput output = run(null,7L,Map.of("compact",true));

        assertEquals(List.of("not authenticated"),output.getFailureMessages());
        Mockito.verifyNoInteractions(commandClient);
        Mockito.verifyNoInteractions(queryClient);
    }

    @Test
    public void
    mergeFailureIsReported()
        throws Exception
    {
        Mockito
            .when(commandClient.mergeUserValues(Mockito.any(MergeUserValuesRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(new MergeUserValuesReply().setSuccess(false).setFailureMessage("db down")));

        PreferencesOutput output = run(7L,null,Map.of("compact",true));

        assertEquals(List.of("db down"),output.getFailureMessages());
    }
}

//////////////////////////////////////////////////////////////////////////////
