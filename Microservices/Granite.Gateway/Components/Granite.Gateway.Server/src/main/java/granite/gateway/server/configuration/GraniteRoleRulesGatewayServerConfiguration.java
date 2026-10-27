//////////////////////////////////////////////////////////////////////////////
// GraniteRoleRulesGatewayServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import java.util.List;

public
class GraniteRoleRulesGatewayServerConfiguration
    extends GraniteGatewayServerConfiguration
{
    @Override
    protected List<String>
    getUsersReaderAuthorities()
    {
        return List.of("ROLE_Admin","ROLE_Auditor","SCOPE_internal");
    }
}

//////////////////////////////////////////////////////////////////////////////
