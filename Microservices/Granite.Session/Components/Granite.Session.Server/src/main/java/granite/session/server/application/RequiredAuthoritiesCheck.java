//////////////////////////////////////////////////////////////////////////////
// RequiredAuthoritiesCheck.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRoles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public
class RequiredAuthoritiesCheck
{
    private final List<String> rejectedAuthorities;

    public
    RequiredAuthoritiesCheck(Collection<String> requiredAuthorities,ApplicationRoles roles)
    {
        Logger       logger   = LogManager.getLogger(RequiredAuthoritiesCheck.class);
        List<String> rejected = new ArrayList<>();

        for (String authority : requiredAuthorities)
        {
            if (authority == null || authority.isBlank())
                continue;

            String trimmed = authority.trim();

            if (!roles.isRole(trimmed))
            {
                rejected.add(trimmed);
                logger.warn(
                    "Required login authority is not a catalog role (ROLE_ + UPPER_SNAKE name expected): {}",
                    trimmed);
            }
        }

        this.rejectedAuthorities = List.copyOf(rejected);
    }

    public List<String>
    getRejectedAuthorities() { return rejectedAuthorities; }
}

//////////////////////////////////////////////////////////////////////////////
