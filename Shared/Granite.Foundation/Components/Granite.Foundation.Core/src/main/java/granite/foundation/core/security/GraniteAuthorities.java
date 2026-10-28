//////////////////////////////////////////////////////////////////////////////
// GraniteAuthorities.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

public
class GraniteAuthorities
{
    public static final String SCOPE_PREFIX   = "SCOPE_";
    public static final String SCOPE_INTERNAL = SCOPE_PREFIX + "internal";

    private
    GraniteAuthorities() {}

    public static boolean
    isScope(String authority)
    {
        return authority != null && authority.startsWith(SCOPE_PREFIX);
    }
}

//////////////////////////////////////////////////////////////////////////////
