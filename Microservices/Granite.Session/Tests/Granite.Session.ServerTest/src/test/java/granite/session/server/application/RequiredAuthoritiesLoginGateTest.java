//////////////////////////////////////////////////////////////////////////////
// RequiredAuthoritiesLoginGateTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class RequiredAuthoritiesLoginGateTest
{
    @Test
    public void
    emptyRequirementPermitsEveryone()
    {
        assertTrue(new RequiredAuthoritiesLoginGate(List.of()).permits(Set.of()));
    }

    @Test
    public void
    blankEntriesAreIgnored()
    {
        assertTrue(new RequiredAuthoritiesLoginGate(List.of("", " ")).permits(Set.of("ROLE_User")));
    }

    @Test
    public void
    anyListedAuthorityPermits()
    {
        ILoginGate gate = new RequiredAuthoritiesLoginGate(List.of("ROLE_Admin","ROLE_Auditor"));

        assertTrue(gate.permits(Set.of("ROLE_User","ROLE_Auditor")));
        assertTrue(gate.permits(Set.of("ROLE_Admin")));
    }

    @Test
    public void
    missingAuthorityIsRefused()
    {
        assertFalse(new RequiredAuthoritiesLoginGate(List.of("ROLE_Admin","ROLE_Auditor")).permits(Set.of("ROLE_User")));
    }

    @Test
    public void
    nullAuthoritiesAreRefused()
    {
        assertFalse(new RequiredAuthoritiesLoginGate(List.of("ROLE_Admin")).permits(null));
    }

    @Test
    public void
    matchingIsCaseSensitive()
    {
        assertFalse(new RequiredAuthoritiesLoginGate(List.of("ROLE_Admin")).permits(Set.of("role_admin")));
    }
}

//////////////////////////////////////////////////////////////////////////////
