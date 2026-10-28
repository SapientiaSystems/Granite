//////////////////////////////////////////////////////////////////////////////
// GraniteAuthoritiesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteAuthoritiesTest
{
    @Test
    public void
    internalScopeIsTheServiceAuthority()
    {
        assertEquals("SCOPE_internal",GraniteAuthorities.SCOPE_INTERNAL);
    }

    @Test
    public void
    recognizesScopeAuthorities()
    {
        assertTrue(GraniteAuthorities.isScope("SCOPE_internal"));
        assertTrue(GraniteAuthorities.isScope("SCOPE_other"));
        assertFalse(GraniteAuthorities.isScope("ROLE_ADMIN"));
        assertFalse(GraniteAuthorities.isScope("scope_internal"));
        assertFalse(GraniteAuthorities.isScope(null));
    }
}

//////////////////////////////////////////////////////////////////////////////
