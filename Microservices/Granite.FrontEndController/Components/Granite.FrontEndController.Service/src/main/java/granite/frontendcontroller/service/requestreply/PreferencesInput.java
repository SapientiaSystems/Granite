//////////////////////////////////////////////////////////////////////////////
// PreferencesInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndInput;

import java.util.LinkedHashMap;
import java.util.Map;

public
class PreferencesInput
    extends FrontEndInput
{
    private Map<String,Object> values;

    public
    PreferencesInput()
    {
        super();
        values = new LinkedHashMap<>();
    }

    @Override
    public PreferencesInput
    setUserId(Long id)
    {
        return (PreferencesInput)super.setUserId(id);
    }

    @Override
    public PreferencesInput
    setSource(String src)
    {
        return (PreferencesInput)super.setSource(src);
    }

    @Override
    public PreferencesInput
    setDestination(String dest)
    {
        return (PreferencesInput)super.setDestination(dest);
    }

    @Override
    public PreferencesInput
    setCommand(String cmd)
    {
        return (PreferencesInput)super.setCommand(cmd);
    }

    public PreferencesInput
    setValues(Map<String,Object> value)
    {
        values = value;
        return this;
    }

    public Map<String,Object>
    getValues() { return values; }
}

//////////////////////////////////////////////////////////////////////////////
