//////////////////////////////////////////////////////////////////////////////
// GraniteRoleRulesGatewayServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;

public
class GraniteRoleRulesGatewayServerConfiguration
    extends GraniteGatewayServerConfiguration
{
    @Override
    protected AuthorizeExchangeSpec
    configureAuthorization(AuthorizeExchangeSpec exchanges,Environment environment)
    {
        AuthorizeExchangeSpec base = super.configureAuthorization(exchanges,environment);

        if (!environment.getProperty("granite.gateway.authorization.role-rules.enabled",Boolean.class,false))
            return base;

        return
            base
                .pathMatchers(
                    HttpMethod.POST,
                    "/frontendcontroller-user-service/get-output",
                    "/frontendcontroller-admin-service/get-output")
                    .hasAnyAuthority("ROLE_ADMIN","ROLE_AUDITOR","SCOPE_internal")
                .pathMatchers("/user-query-service/**")
                    .hasAnyAuthority("ROLE_ADMIN","ROLE_AUDITOR","SCOPE_internal")
                .pathMatchers(HttpMethod.POST,"/session-command-service/logout")
                    .hasAnyAuthority("ROLE_ADMIN","ROLE_AUDITOR","SCOPE_internal")
                .pathMatchers("/session-query-service/**")
                    .hasAnyAuthority("ROLE_ADMIN","ROLE_AUDITOR","SCOPE_internal")
                .pathMatchers("/**")
                    .hasAnyAuthority("ROLE_ADMIN","SCOPE_internal");
    }
}

//////////////////////////////////////////////////////////////////////////////
