//////////////////////////////////////////////////////////////////////////////
// OktaRoles.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRoles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
class OktaRoles
{
    private final OktaSettings     settings;
    private final ApplicationRoles roles;

    public
    OktaRoles(OktaSettings settings,ApplicationRoles roles)
    {
        this.settings = settings;
        this.roles    = roles;

        Logger logger = LogManager.getLogger(OktaRoles.class);

        for (Map.Entry<String,String> entry : settings.getGroupMapping().entrySet())
            if (entry.getValue() == null || !roles.isRole(entry.getValue().trim()))
                logger.warn("Okta group mapping ignored: not a catalog role (group: {})",entry.getKey());
    }

    public Optional<Set<String>>
    rolesFor(Set<String> groups)
    {
        Set<String> mapped = new LinkedHashSet<>();

        if (groups != null)
            for (String group : groups)
            {
                String role = settings.getGroupMapping().get(group);

                if (role != null && roles.isRole(role.trim()))
                    mapped.add(role.trim());
            }

        if (!mapped.isEmpty())
            return Optional.of(roles.expand(mapped));

        if (settings.getUnmappedGroups() == OktaUnmappedGroups.REFUSE)
            return Optional.empty();

        return Optional.of(roles.expand(Set.of(roles.getDefaultRole())));
    }
}

//////////////////////////////////////////////////////////////////////////////
