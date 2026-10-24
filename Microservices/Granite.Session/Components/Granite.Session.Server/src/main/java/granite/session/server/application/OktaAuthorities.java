//////////////////////////////////////////////////////////////////////////////
// OktaAuthorities.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.util.LinkedHashSet;
import java.util.Set;

public
class OktaAuthorities
{
    private final OktaSettings settings;

    public
    OktaAuthorities(OktaSettings settings)
    {
        this.settings = settings;
    }

    public Set<String>
    calculate(Set<String> userAuthorities,Set<String> groups)
    {
        Set<String> result = new LinkedHashSet<>();

        if (settings.getAuthoritiesSource() != OktaAuthoritiesSource.OKTA_GROUPS && userAuthorities != null)
            result.addAll(userAuthorities);

        if (settings.getAuthoritiesSource() != OktaAuthoritiesSource.USER && groups != null)
            for (String group : groups)
            {
                String role = settings.getGroupMapping().get(group);

                if (role != null && !role.isBlank())
                    result.add(role.trim());
            }

        return result;
    }
}

//////////////////////////////////////////////////////////////////////////////
