//////////////////////////////////////////////////////////////////////////////
// GraniteApplicationRoles.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import java.util.List;
import java.util.Set;

public
class GraniteApplicationRoles
{
    public static final String AUDITOR = "ROLE_AUDITOR";
    public static final String USER    = "ROLE_USER";

    private
    GraniteApplicationRoles() {}

    public static ApplicationRolesManager
    create()
    {
        return
            new ApplicationRolesManager(
                List.of(
                    new RoleDefinition(AUDITOR,"Auditor",Set.of()),
                    new RoleDefinition(USER,"User",Set.of())),
                USER);
    }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
