//////////////////////////////////////////////////////////////////////////////
// UnauthenticatedUsersOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.frontendcontroller.service.requestreply.UsersOutput;

import java.util.ArrayList;
import java.util.List;

public
class UnauthenticatedUsersOutput
{
    public static final String MESSAGE = "not authenticated";

    private
    UnauthenticatedUsersOutput() {}

    public static UsersOutput
    create(ApplicationRolesManager applicationRoles)
    {
        return
            new UsersOutput()
                .setUsers(List.of())
                .setFailureMessages(new ArrayList<>(List.of(MESSAGE)))
                .setRoles(RoleCatalogData.from(applicationRoles));
    }
}

//////////////////////////////////////////////////////////////////////////////
