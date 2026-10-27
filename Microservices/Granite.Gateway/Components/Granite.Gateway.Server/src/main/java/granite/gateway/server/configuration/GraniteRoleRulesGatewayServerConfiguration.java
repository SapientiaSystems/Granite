//////////////////////////////////////////////////////////////////////////////
// GraniteRoleRulesGatewayServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import org.springframework.core.env.Environment;

import java.util.List;

public
class GraniteRoleRulesGatewayServerConfiguration
    extends GraniteGatewayServerConfiguration
{
    @Override
    protected List<String>
    getUsersReaderAuthorities(Environment environment)
    {
        if (!environment.getProperty("granite.gateway.authorization.role-rules.enabled",Boolean.class,false))
            return super.getUsersReaderAuthorities(environment);

        return List.of(ApplicationRolesManager.ADMIN,GraniteApplicationRolesManagerSupplier.AUDITOR,"SCOPE_internal");
    }
}

//////////////////////////////////////////////////////////////////////////////
