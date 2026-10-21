//////////////////////////////////////////////////////////////////////////////
// RequiredAuthoritiesLoginGate.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public
class RequiredAuthoritiesLoginGate
    implements ILoginGate
{
    private final Set<String> requiredAuthorities;

    public
    RequiredAuthoritiesLoginGate(Collection<String> requiredAuthorities)
    {
        Objects.requireNonNull(requiredAuthorities,"requiredAuthorities");

        Set<String> required = new HashSet<>();

        for (String authority : requiredAuthorities)
        {
            if (authority != null && !authority.isBlank())
                required.add(authority.trim());
        }

        this.requiredAuthorities = Collections.unmodifiableSet(required);
    }

    @Override
    public boolean
    permits(Collection<String> authorities)
    {
        if (requiredAuthorities.isEmpty())
            return true;

        if (authorities == null)
            return false;

        for (String authority : authorities)
        {
            if (requiredAuthorities.contains(authority))
                return true;
        }

        return false;
    }
}

//////////////////////////////////////////////////////////////////////////////
