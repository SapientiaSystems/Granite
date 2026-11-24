//////////////////////////////////////////////////////////////////////////////
// ClientAuthorityPolicy.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteAuthorities;

import java.util.LinkedHashSet;
import java.util.Set;

public
class ClientAuthorityPolicy
{
    private final ApplicationRolesManager roles;

    public
    ClientAuthorityPolicy(ApplicationRolesManager r)
    {
        roles = r;
    }

    public Set<String>
    validateAdded(Set<String> previous,Set<String> requested)
    {
        Set<String> wanted = requested == null ? new LinkedHashSet<>() : new LinkedHashSet<>(requested);
        Set<String> had    = previous == null ? new LinkedHashSet<>() : previous;

        for (String authority : wanted)
        {
            if (had.contains(authority))
                continue;

            if (authority == null || GraniteAuthorities.isScope(authority) || ApplicationRolesManager.ADMIN.equals(authority))
                throw new IllegalArgumentException("role not allowed for clients: " + authority);

            if (!roles.isRole(authority))
                throw new IllegalArgumentException("unknown role: " + authority);

            if (roles.expand(Set.of(authority)).contains(ApplicationRolesManager.ADMIN))
                throw new IllegalArgumentException("role not allowed for clients: " + authority);
        }

        return roles.expand(wanted);
    }

    public Set<String>
    mintable(Set<String> stored)
    {
        Set<String> result = new LinkedHashSet<>();

        if (stored == null)
            return result;

        for (String authority : stored)
            if (roles.isRole(authority)
                && !ApplicationRolesManager.ADMIN.equals(authority)
                && !GraniteAuthorities.isScope(authority))
                result.add(authority);

        return result;
    }
}

//////////////////////////////////////////////////////////////////////////////
