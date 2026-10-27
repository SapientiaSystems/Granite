//////////////////////////////////////////////////////////////////////////////
// CatalogRoleLoginGate.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRoles;

import java.util.Collection;
import java.util.Objects;

public
class CatalogRoleLoginGate
    implements ILoginGate
{
    private final ApplicationRoles roles;

    public
    CatalogRoleLoginGate(ApplicationRoles roles)
    {
        this.roles = Objects.requireNonNull(roles,"roles");
    }

    @Override
    public boolean
    permits(Collection<String> authorities)
    {
        if (authorities == null)
            return false;

        for (String authority : authorities)
        {
            if (authority != null && roles.isRole(authority))
                return true;
        }

        return false;
    }
}

//////////////////////////////////////////////////////////////////////////////
