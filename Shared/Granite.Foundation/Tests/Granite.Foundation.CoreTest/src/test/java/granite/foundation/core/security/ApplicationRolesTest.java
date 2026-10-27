//////////////////////////////////////////////////////////////////////////////
// ApplicationRolesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CommitStage")
public
class ApplicationRolesTest
{
    private static final String REVIEWER = "ROLE_PromotionsReviewer";
    private static final String PLANNER  = "ROLE_PromotionsPlanner";
    private static final String APPROVER = "ROLE_PromotionsApprover";

    @Test
    public void
    expandAddsTransitiveImplications()
    {
        assertEquals(Set.of(APPROVER,PLANNER,REVIEWER),poplar().expand(Set.of(APPROVER)));
    }

    @Test
    public void
    expandKeepsUnknownValues()
    {
        assertEquals(Set.of("SCOPE_internal"),poplar().expand(Set.of("SCOPE_internal")));
    }

    @Test
    public void
    adminIsAlwaysPresentAndSeparate()
    {
        ApplicationRoles subject = poplar();

        assertEquals(ApplicationRoles.ADMIN,subject.getRoles().get(0).getName());
        assertEquals(Set.of(ApplicationRoles.ADMIN),subject.expand(Set.of(ApplicationRoles.ADMIN)));
    }

    @Test
    public void
    dependentsOfReviewer()
    {
        assertEquals(Set.of(PLANNER,APPROVER),poplar().dependentsOf(REVIEWER));
    }

    @Test
    public void
    rejectsCycle()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRoles(
                    List.of(
                        new RoleDefinition("ROLE_A","A",Set.of("ROLE_B")),
                        new RoleDefinition("ROLE_B","B",Set.of("ROLE_A"))),
                    "ROLE_A"));
    }

    @Test
    public void
    rejectsUnknownImplied()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRoles(
                    List.of(new RoleDefinition("ROLE_A","A",Set.of("ROLE_Missing"))),
                    "ROLE_A"));
    }

    @Test
    public void
    rejectsDefaultOutsideCatalog()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRoles(
                    List.of(new RoleDefinition("ROLE_A","A",Set.of())),
                    "ROLE_Other"));
    }

    @Test
    public void
    rejectsImplyingAdmin()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRoles(
                    List.of(new RoleDefinition("ROLE_A","A",Set.of(ApplicationRoles.ADMIN))),
                    "ROLE_A"));
    }

    @Test
    public void
    rejectsApplicationRoleNamedAdmin()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRoles(
                    List.of(new RoleDefinition(ApplicationRoles.ADMIN,"Admin",Set.of())),
                    ApplicationRoles.ADMIN));
    }

    @Test
    public void
    rejectsDuplicateNames()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRoles(
                    List.of(
                        new RoleDefinition("ROLE_A","A",Set.of()),
                        new RoleDefinition("ROLE_A","Other A",Set.of())),
                    "ROLE_A"));
    }

    @Test
    public void
    rejectsAdminAsDefault()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRoles(
                    List.of(new RoleDefinition("ROLE_A","A",Set.of())),
                    ApplicationRoles.ADMIN));
    }

    @Test
    public void
    expandTreatsNullAsEmpty()
    {
        assertEquals(Set.of(),poplar().expand(null));
    }

    @Test
    public void
    graniteCatalogHasAuditorAndUser()
    {
        ApplicationRoles subject = GraniteApplicationRoles.create();

        assertEquals(
            List.of("ROLE_Admin","ROLE_Auditor","ROLE_User"),
            subject.getRoles().stream().map(RoleDefinition::getName).toList());
        assertEquals("ROLE_User",subject.getDefaultRole());
    }

    private ApplicationRoles
    poplar()
    {
        return
            new ApplicationRoles(
                List.of(
                    new RoleDefinition(REVIEWER,"Reviewer",Set.of()),
                    new RoleDefinition(PLANNER,"Planner",Set.of(REVIEWER)),
                    new RoleDefinition(APPROVER,"Approver",Set.of(PLANNER))),
                REVIEWER);
    }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
