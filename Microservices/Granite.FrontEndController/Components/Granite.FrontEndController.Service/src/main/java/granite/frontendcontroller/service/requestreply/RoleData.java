//////////////////////////////////////////////////////////////////////////////
// RoleData.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public
class RoleData
    implements Serializable
{
    private String       name;
    private String       label;
    private List<String> impliedRoles;

    public
    RoleData()
    {
        name         = null;
        label        = null;
        impliedRoles = new ArrayList<>();
    }

    public RoleData
    setName(String name)
    {
        this.name = name;
        return this;
    }

    public RoleData
    setLabel(String label)
    {
        this.label = label;
        return this;
    }

    public RoleData
    setImpliedRoles(List<String> impliedRoles)
    {
        this.impliedRoles = impliedRoles;
        return this;
    }

    public String
    getName() { return name; }

    public String
    getLabel() { return label; }

    public List<String>
    getImpliedRoles() { return impliedRoles; }
}

//////////////////////////////////////////////////////////////////////////////
