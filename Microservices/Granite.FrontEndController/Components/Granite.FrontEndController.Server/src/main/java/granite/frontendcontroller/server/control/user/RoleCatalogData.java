//////////////////////////////////////////////////////////////////////////////
// RoleCatalogData.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.RoleDefinition;
import granite.frontendcontroller.service.requestreply.RoleData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public
class RoleCatalogData
{
    private
    RoleCatalogData() {}

    public static List<RoleData>
    from(ApplicationRolesManager applicationRoles)
    {
        List<RoleData> roles = new ArrayList<>();

        for (RoleDefinition role : applicationRoles.getRoles())
        {
            List<String> implied = new ArrayList<>(role.getImpliedRoles());

            Collections.sort(implied);
            roles.add(new RoleData().setName(role.getName()).setLabel(role.getLabel()).setImpliedRoles(implied));
        }

        return roles;
    }
}

//////////////////////////////////////////////////////////////////////////////
