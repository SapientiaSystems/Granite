//////////////////////////////////////////////////////////////////////////////
// CatalogRoleLoginGateTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class CatalogRoleLoginGateTest
{
    private final ILoginGate gate = new CatalogRoleLoginGate(new GraniteApplicationRolesManagerSupplier().get());

    @Test
    public void
    permitsCatalogRole()
    {
        assertTrue(gate.permits(Set.of("ROLE_USER")));
        assertTrue(gate.permits(Set.of("SCOPE_internal","ROLE_ADMIN")));
    }

    @Test
    public void
    refusesEmptyAndNonCatalogAuthorities()
    {
        assertFalse(gate.permits(Set.of()));
        assertFalse(gate.permits(null));
        assertFalse(gate.permits(Set.of("SCOPE_internal")));
        assertFalse(gate.permits(Set.of("ROLE_User")));
    }
}

//////////////////////////////////////////////////////////////////////////////
