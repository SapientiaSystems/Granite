//////////////////////////////////////////////////////////////////////////////
// OktaRolesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRoles;
import granite.foundation.core.security.RoleDefinition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
public
class OktaRolesTest
{
    private static final ApplicationRoles ROLES =
        new ApplicationRoles(
            List.of(
                new RoleDefinition("ROLE_AUDITOR","Auditor",Set.of()),
                new RoleDefinition("ROLE_USER","User",Set.of()),
                new RoleDefinition("ROLE_PROMOTIONS_APPROVER","Promotions Approver",Set.of("ROLE_AUDITOR","ROLE_USER"))),
            "ROLE_USER");

    private static final Map<String,String> MAPPING =
        Map.of(
            "GC-Approvers","ROLE_PROMOTIONS_APPROVER",
            "GC-Admins","ROLE_ADMIN",
            "GC-Bogus","ROLE_Nope");

    @Test
    public void
    defaultsAreOnCreateAndDefaultRole()
    {
        OktaSettings settings = new OktaSettings();

        assertEquals(OktaRoleAssignment.ON_CREATE,settings.getRoleAssignment());
        assertEquals(OktaUnmappedGroups.DEFAULT_ROLE,settings.getUnmappedGroups());
    }

    @Test
    public void
    mappedGroupsExpand()
    {
        assertEquals(
            Optional.of(Set.of("ROLE_PROMOTIONS_APPROVER","ROLE_AUDITOR","ROLE_USER")),
            subject(OktaUnmappedGroups.DEFAULT_ROLE).rolesFor(Set.of("GC-Approvers","Everyone")));
    }

    @Test
    public void
    unmappedFallsBackToDefault()
    {
        assertEquals(
            Optional.of(Set.of("ROLE_USER")),
            subject(OktaUnmappedGroups.DEFAULT_ROLE).rolesFor(Set.of("Everyone")));
        assertEquals(
            Optional.of(Set.of("ROLE_USER")),
            subject(OktaUnmappedGroups.DEFAULT_ROLE).rolesFor(null));
    }

    @Test
    public void
    refuseModeReturnsEmpty()
    {
        assertEquals(Optional.empty(),subject(OktaUnmappedGroups.REFUSE).rolesFor(Set.of("Everyone")));
        assertEquals(Optional.empty(),subject(OktaUnmappedGroups.REFUSE).rolesFor(Set.of()));
    }

    @Test
    public void
    nonCatalogMappingIgnored()
    {
        assertEquals(
            Optional.of(Set.of("ROLE_USER")),
            subject(OktaUnmappedGroups.DEFAULT_ROLE).rolesFor(Set.of("GC-Bogus")));
        assertEquals(Optional.empty(),subject(OktaUnmappedGroups.REFUSE).rolesFor(Set.of("GC-Bogus")));
    }

    @Test
    public void
    adminMappingIsAccepted()
    {
        assertEquals(
            Optional.of(Set.of(ApplicationRoles.ADMIN)),
            subject(OktaUnmappedGroups.REFUSE).rolesFor(Set.of("GC-Admins")));
    }

    private OktaRoles
    subject(OktaUnmappedGroups unmapped)
    {
        return
            new OktaRoles(
                new OktaSettings().setGroupMapping(MAPPING).setUnmappedGroups(unmapped),
                ROLES);
    }
}

//////////////////////////////////////////////////////////////////////////////
