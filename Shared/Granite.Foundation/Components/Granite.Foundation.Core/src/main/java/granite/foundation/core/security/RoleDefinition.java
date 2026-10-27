//////////////////////////////////////////////////////////////////////////////
// RoleDefinition.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import java.util.Objects;
import java.util.Set;

public
class RoleDefinition
{
    private final String      name;
    private final String      label;
    private final Set<String> impliedRoles;

    public
    RoleDefinition(String name,String label,Set<String> impliedRoles)
    {
        this.name         = Objects.requireNonNull(name,"name");
        this.label        = Objects.requireNonNull(label,"label");
        this.impliedRoles = Set.copyOf(Objects.requireNonNull(impliedRoles,"impliedRoles"));
    }

    public String
    getName() { return name; }

    public String
    getLabel() { return label; }

    public Set<String>
    getImpliedRoles() { return impliedRoles; }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
