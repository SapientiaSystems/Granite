//////////////////////////////////////////////////////////////////////////////
// OktaGroupMappingCheck.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRoles;
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
    OktaGroupMappingCheck(OktaSettings settings,ApplicationRoles roles)
    {
        Logger       logger  = LogManager.getLogger(OktaGroupMappingCheck.class);
        List<String> ignored = new ArrayList<>();

        for (Map.Entry<String,String> entry : settings.getGroupMapping().entrySet())
            if (entry.getValue() == null || !roles.isRole(entry.getValue().trim()))
            {
                ignored.add(entry.getKey());
                logger.warn("Okta group mapping ignored: not a catalog role (group: {})",entry.getKey());
            }

        this.ignoredGroups = List.copyOf(ignored);
    }

    public List<String>
    getIgnoredGroups() { return ignoredGroups; }
}

//////////////////////////////////////////////////////////////////////////////
