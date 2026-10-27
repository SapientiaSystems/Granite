//////////////////////////////////////////////////////////////////////////////
// PreferenceDefinitionData.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public
class PreferenceDefinitionData
    implements Serializable
{
    private String       key;
    private String       type;
    private Object       defaultValue;
    private List<Object> allowedValues;

    public
    PreferenceDefinitionData()
    {
        key           = null;
        type          = null;
        defaultValue  = null;
        allowedValues = new ArrayList<>();
    }

    public PreferenceDefinitionData
    setKey(String value)
    {
        key = value;
        return this;
    }

    public PreferenceDefinitionData
    setType(String value)
    {
        type = value;
        return this;
    }

    public PreferenceDefinitionData
    setDefaultValue(Object value)
    {
        defaultValue = value;
        return this;
    }

    public PreferenceDefinitionData
    setAllowedValues(List<Object> value)
    {
        allowedValues = value;
        return this;
    }

    public String
    getKey() { return key; }

    public String
    getType() { return type; }

    public Object
    getDefaultValue() { return defaultValue; }

    public List<Object>
    getAllowedValues() { return allowedValues; }
}

//////////////////////////////////////////////////////////////////////////////
