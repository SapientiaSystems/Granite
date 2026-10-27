//////////////////////////////////////////////////////////////////////////////
// PoplarUserCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.configuration;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.foundation.core.security.RoleDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
public
class PoplarUserCommandServerConfiguration
    extends ExtendedUserCommandServerConfiguration
{
    public static final String REVIEWER = "ROLE_PROMOTIONS_REVIEWER";
    public static final String APPROVER = "ROLE_PROMOTIONS_APPROVER";

    @Override
    @Bean
    @Scope("singleton")
    public ApplicationRolesManager
    applicationRoles()
    {
        return poplarRoles();
    }

    public static ApplicationRolesManager
    poplarRoles()
    {
        List<RoleDefinition> roles = new ArrayList<>();

        for (RoleDefinition role : new GraniteApplicationRolesManagerSupplier().get().getRoles())
            if (!ApplicationRolesManager.ADMIN.equals(role.getName()))
                roles.add(role);

        roles.add(new RoleDefinition(REVIEWER,"Promotions Reviewer",Set.of(GraniteApplicationRolesManagerSupplier.USER)));
        roles.add(new RoleDefinition(APPROVER,"Promotions Approver",Set.of(REVIEWER)));

        return new ApplicationRolesManager(roles,GraniteApplicationRolesManagerSupplier.USER);
    }
}

//////////////////////////////////////////////////////////////////////////////
