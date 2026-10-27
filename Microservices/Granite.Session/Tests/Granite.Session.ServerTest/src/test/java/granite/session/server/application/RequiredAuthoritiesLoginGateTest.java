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
        assertTrue(new RequiredAuthoritiesLoginGate(List.of("", " ")).permits(Set.of("ROLE_USER")));
    }

    @Test
    public void
    anyListedAuthorityPermits()
    {
        ILoginGate gate = new RequiredAuthoritiesLoginGate(List.of("ROLE_ADMIN","ROLE_AUDITOR"));

        assertTrue(gate.permits(Set.of("ROLE_USER","ROLE_AUDITOR")));
        assertTrue(gate.permits(Set.of("ROLE_ADMIN")));
    }

    @Test
    public void
    missingAuthorityIsRefused()
    {
        assertFalse(new RequiredAuthoritiesLoginGate(List.of("ROLE_ADMIN","ROLE_AUDITOR")).permits(Set.of("ROLE_USER")));
    }

    @Test
    public void
    nullAuthoritiesAreRefused()
    {
        assertFalse(new RequiredAuthoritiesLoginGate(List.of("ROLE_ADMIN")).permits(null));
    }

    @Test
    public void
    matchingIsCaseSensitive()
    {
        assertFalse(new RequiredAuthoritiesLoginGate(List.of("ROLE_ADMIN")).permits(Set.of("role_admin")));
    }
}

//////////////////////////////////////////////////////////////////////////////
