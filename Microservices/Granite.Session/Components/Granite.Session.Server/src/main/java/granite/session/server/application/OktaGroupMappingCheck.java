//////////////////////////////////////////////////////////////////////////////
// OktaGroupMappingCheck.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRolesManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public
class OktaGroupMappingCheck
{
    private final List<String> ignoredGroups;

    public
    OktaGroupMappingCheck(OktaSettings settings,ApplicationRolesManager roles)
    {
        Logger       logger  = LogManager.getLogger(OktaGroupMappingCheck.class);
        List<String> ignored = new ArrayList<>();

        for (Map.Entry<String,String> entry : settings.getGroupMapping().entrySet())
            if (entry.getValue() == null || !roles.isRole(entry.getValue().trim()))
            {
                ignored.add(entry.getKey());
                logger.warn("Okta group mapping ignored: role values must be catalog role names in ROLE_ + UPPER_SNAKE form (group: {})",entry.getKey());
            }

        this.ignoredGroups = List.copyOf(ignored);
    }

    public List<String>
    getIgnoredGroups() { return ignoredGroups; }
}

//////////////////////////////////////////////////////////////////////////////
