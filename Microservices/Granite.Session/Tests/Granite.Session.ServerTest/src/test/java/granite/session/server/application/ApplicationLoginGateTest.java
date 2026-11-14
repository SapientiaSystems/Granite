//////////////////////////////////////////////////////////////////////////////
// ApplicationLoginGateTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ApplicationLoginGateTest
{
    private final ILoginGate catalogGate = new CatalogRoleLoginGate(new GraniteApplicationRolesManagerSupplier().get());
    private final Map<String,ILoginGate> gates = Map.of("client",new RequiredAuthoritiesLoginGate(List.of("ROLE_CLIENT_ADMIN")));

    private final IApplicationLoginGate subject =
        new ApplicationLoginGate(List.of("granite","client"),catalogGate,gates);

    @Test
    public void
    graniteLoginRefusesClientAdminOnly()
    {
        assertFalse(subject.permits("granite",Set.of("ROLE_CLIENT_ADMIN")));
        assertTrue(subject.permits("granite",Set.of("ROLE_USER","ROLE_CLIENT_ADMIN")));
        assertTrue(subject.permits("granite",Set.of("ROLE_USER")));
    }

    @Test
    public void
    graniteLoginWithExplicitRequiredAuthoritiesRefusesClientAdminOnly()
    {
        IApplicationLoginGate explicit =
            new ApplicationLoginGate(List.of("granite","client"),new RequiredAuthoritiesLoginGate(List.of()),gates);

        assertFalse(explicit.permits("granite",Set.of("ROLE_CLIENT_ADMIN")));
        assertTrue(explicit.permits("granite",Set.of("ROLE_USER","ROLE_CLIENT_ADMIN")));
    }

    @Test
    public void
    clientLoginRequiresClientAdmin()
    {
        assertFalse(subject.permits("client",Set.of("ROLE_ADMIN")));
        assertFalse(subject.permits("client",Set.of()));
        assertTrue(subject.permits("client",Set.of("ROLE_CLIENT_ADMIN")));
    }

    @Test
    public void
    otherApplicationWithoutOwnGateUsesGraniteRuleWithoutClientAdmin()
    {
        IApplicationLoginGate custom = new ApplicationLoginGate(List.of("granite","client","foo"),catalogGate,gates);

        assertFalse(custom.permits("foo",Set.of("ROLE_CLIENT_ADMIN")));
        assertTrue(custom.permits("foo",Set.of("ROLE_USER")));
        assertTrue(custom.permits("foo",Set.of("ROLE_USER","ROLE_CLIENT_ADMIN")));
    }

    @Test
    public void
    clientLoginWithEmptyConfiguredListStillRequiresClientAdmin()
    {
        IApplicationLoginGate empty =
            new ApplicationLoginGate(
                List.of("granite","client"),
                catalogGate,
                Map.of("client",new RequiredAuthoritiesLoginGate(List.of())));

        assertFalse(empty.permits("client",Set.of("ROLE_ADMIN")));
        assertTrue(empty.permits("client",Set.of("ROLE_CLIENT_ADMIN")));
    }

    @Test
    public void
    clientLoginWithoutAnyGateStillRequiresClientAdmin()
    {
        IApplicationLoginGate none = new ApplicationLoginGate(List.of("granite","client"),catalogGate,Map.of());

        assertFalse(none.permits("client",Set.of("ROLE_ADMIN")));
        assertTrue(none.permits("client",Set.of("ROLE_CLIENT_ADMIN")));
    }

    @Test
    public void
    nullApplicationMeansGraniteAndUnknownIsRefused()
    {
        assertTrue(subject.permits(null,Set.of("ROLE_USER")));
        assertTrue(subject.isKnown(null));
        assertFalse(subject.isKnown("other"));
        assertFalse(subject.permits("other",Set.of("ROLE_USER")));
        assertFalse(subject.permits("granite",null));
    }
}

//////////////////////////////////////////////////////////////////////////////
