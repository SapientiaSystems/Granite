//////////////////////////////////////////////////////////////////////////////
// PreferenceDefinition.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import java.util.List;
import java.util.Objects;

public
class PreferenceDefinition
{
    private final String         key;
    private final PreferenceType type;
    private final Object         defaultValue;
    private final List<Object>   allowedValues;

    public
    PreferenceDefinition(String key,PreferenceType type,Object defaultValue,List<Object> allowedValues)
    {
        this.key           = Objects.requireNonNull(key,"key");
        this.type          = Objects.requireNonNull(type,"type");
        this.defaultValue  = Objects.requireNonNull(defaultValue,"defaultValue");
        this.allowedValues = allowedValues == null ? List.of() : List.copyOf(allowedValues);

        if (key.isBlank())
            throw new IllegalArgumentException("preference key must not be blank");

        if (type == PreferenceType.ENUM && this.allowedValues.isEmpty())
            throw new IllegalArgumentException("preference " + key + " is an ENUM without allowed values");

        if (!isValid(defaultValue))
            throw new IllegalArgumentException("preference " + key + " has an invalid default value");
    }

    public static PreferenceDefinition
    string(String key,String defaultValue)
    {
        return new PreferenceDefinition(key,PreferenceType.STRING,defaultValue,List.of());
    }

    public static PreferenceDefinition
    bool(String key,boolean defaultValue)
    {
        return new PreferenceDefinition(key,PreferenceType.BOOLEAN,defaultValue,List.of());
    }

    public static PreferenceDefinition
    number(String key,Number defaultValue)
    {
        return new PreferenceDefinition(key,PreferenceType.NUMBER,defaultValue,List.of());
    }

    public static PreferenceDefinition
    enumeration(String key,List<String> allowedValues,String defaultValue)
    {
        return new PreferenceDefinition(key,PreferenceType.ENUM,defaultValue,List.copyOf(allowedValues));
    }

    public boolean
    isValid(Object value)
    {
        switch (type)
        {
            case STRING:
                return value instanceof String;
            case BOOLEAN:
                return value instanceof Boolean;
            case NUMBER:
                return value instanceof Number;
            case ENUM:
                return value instanceof String && allowedValues.contains(value);
            default:
                return false;
        }
    }

    public String
    getKey() { return key; }

    public PreferenceType
    getType() { return type; }

    public Object
    getDefaultValue() { return defaultValue; }

    public List<Object>
    getAllowedValues() { return allowedValues; }
}

//////////////////////////////////////////////////////////////////////////////
