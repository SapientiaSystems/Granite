//////////////////////////////////////////////////////////////////////////////
// PreferencesAssembler.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.preference;

import granite.frontendcontroller.server.application.PreferenceDefinition;
import granite.frontendcontroller.server.application.PreferenceType;
import granite.frontendcontroller.service.requestreply.PreferenceDefinitionData;
import granite.frontendcontroller.service.requestreply.PreferencesOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.UserKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public
class PreferencesAssembler
{
    public static final String PREFIX = "preferences.";

    private static final String UNREADABLE = "unable to read preferences";

    private final IUserQueryServiceClient    queryClient;
    private final List<PreferenceDefinition> definitions;

    public
    PreferencesAssembler(IUserQueryServiceClient queryClient,List<PreferenceDefinition> definitions)
    {
        this.queryClient = queryClient;
        this.definitions = List.copyOf(definitions);
    }

    public CompletionStage<PreferencesOutput>
    read(Long userId,List<String> failures)
    {
        return
            queryClient
                .findUsersByKey(new FindUsersByKeyRequest().setKey(UserKey.ofUserId(userId)).setReturnIfFound(true))
                .thenApply(reply -> toOutput(storedValues(reply,failures),failures))
                .exceptionally(
                    e ->
                    {
                        failures.add(UNREADABLE);
                        return toOutput(Map.of(),failures);
                    });
    }

    public PreferencesOutput
    unauthenticated()
    {
        return
            new PreferencesOutput()
                .setDefinitions(definitionData())
                .setFailureMessages(new ArrayList<>(List.of("not authenticated")));
    }

    public Map<String,Object>
    validate(Map<String,Object> incoming,List<String> failures)
    {
        Map<String,Object> entries = new LinkedHashMap<>();

        if (incoming == null)
            return entries;

        for (Map.Entry<String,Object> entry : incoming.entrySet())
        {
            PreferenceDefinition definition = find(entry.getKey());

            if (definition == null)
                failures.add(entry.getKey() + ": not a declared preference");
            else if (entry.getValue() != null && !definition.isValid(entry.getValue()))
                failures.add(entry.getKey() + ": invalid value");
            else
                entries.put(PREFIX + entry.getKey(),entry.getValue());
        }

        return entries;
    }

    protected PreferencesOutput
    toOutput(Map<String,Object> stored,List<String> failures)
    {
        Map<String,Object> values = new LinkedHashMap<>();

        for (PreferenceDefinition definition : definitions)
        {
            Object value = normalise(definition,stored.get(PREFIX + definition.getKey()));

            values.put(definition.getKey(),value != null && definition.isValid(value) ? value : definition.getDefaultValue());
        }

        return
            new PreferencesOutput()
                .setValues(values)
                .setDefinitions(definitionData())
                .setFailureMessages(failures);
    }

    protected Map<String,Object>
    storedValues(FindUsersByKeyReply reply,List<String> failures)
    {
        if (!reply.isSuccess() || reply.getFoundUsers() == null || reply.getFoundUsers().isEmpty())
        {
            failures.add(UNREADABLE);
            return Map.of();
        }

        Map<String,Object> values = reply.getFoundUsers().get(0).getValues();

        return values == null ? Map.of() : values;
    }

    protected List<PreferenceDefinitionData>
    definitionData()
    {
        List<PreferenceDefinitionData> data = new ArrayList<>();

        for (PreferenceDefinition definition : definitions)
            data.add(
                new PreferenceDefinitionData()
                    .setKey(definition.getKey())
                    .setType(definition.getType().name())
                    .setDefaultValue(definition.getDefaultValue())
                    .setAllowedValues(new ArrayList<>(definition.getAllowedValues())));

        return data;
    }

    private Object
    normalise(PreferenceDefinition definition,Object stored)
    {
        if (definition.getType() == PreferenceType.BOOLEAN && stored instanceof String text)
        {
            if (text.equalsIgnoreCase("true"))
                return Boolean.TRUE;

            if (text.equalsIgnoreCase("false"))
                return Boolean.FALSE;
        }

        return stored;
    }

    private PreferenceDefinition
    find(String key)
    {
        for (PreferenceDefinition definition : definitions)
            if (definition.getKey().equals(key))
                return definition;

        return null;
    }
}

//////////////////////////////////////////////////////////////////////////////
