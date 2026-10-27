//////////////////////////////////////////////////////////////////////////////
// GraniteApplicationRolesManagerSupplier.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public
class GraniteApplicationRolesManagerSupplier
    implements Supplier<ApplicationRolesManager>
{
    public static final String AUDITOR = "ROLE_AUDITOR";
    public static final String USER    = "ROLE_USER";

    public
    GraniteApplicationRolesManagerSupplier() {}

    @Override
    public ApplicationRolesManager
    get()
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
