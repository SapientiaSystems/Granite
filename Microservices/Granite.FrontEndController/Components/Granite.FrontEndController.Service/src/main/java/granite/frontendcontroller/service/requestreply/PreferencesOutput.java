//////////////////////////////////////////////////////////////////////////////
// PreferencesOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public
class PreferencesOutput
    extends FrontEndOutput
{
    private Map<String,Object>             values;
    private List<PreferenceDefinitionData> definitions;
    private List<String>                   failureMessages;

    public
    PreferencesOutput()
    {
        super();
        values          = new LinkedHashMap<>();
        definitions     = new ArrayList<>();
        failureMessages = new ArrayList<>();
    }

    @Override
    public PreferencesOutput
    setUserId(Long id)
    {
        return (PreferencesOutput)super.setUserId(id);
    }

    @Override
    public PreferencesOutput
    setSource(String src)
    {
        return (PreferencesOutput)super.setSource(src);
    }

    @Override
    public PreferencesOutput
    setDestination(String dest)
    {
        return (PreferencesOutput)super.setDestination(dest);
    }

    @Override
    public PreferencesOutput
    setCommand(String cmd)
    {
        return (PreferencesOutput)super.setCommand(cmd);
    }

    public PreferencesOutput
    setValues(Map<String,Object> value)
    {
        values = value;
        return this;
    }

    public PreferencesOutput
    setDefinitions(List<PreferenceDefinitionData> value)
    {
        definitions = value;
        return this;
    }

    public PreferencesOutput
    setFailureMessages(List<String> value)
    {
        failureMessages = value;
        return this;
    }

    public Map<String,Object>
    getValues() { return values; }

    public List<PreferenceDefinitionData>
    getDefinitions() { return definitions; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
