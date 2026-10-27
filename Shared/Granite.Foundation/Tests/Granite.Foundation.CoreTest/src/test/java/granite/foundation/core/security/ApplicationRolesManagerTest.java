//////////////////////////////////////////////////////////////////////////////
// ApplicationRolesManagerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CommitStage")
public
class ApplicationRolesManagerTest
{
    private static final String REVIEWER = "ROLE_PROMOTIONS_REVIEWER";
    private static final String PLANNER  = "ROLE_PROMOTIONS_PLANNER";
    private static final String APPROVER = "ROLE_PROMOTIONS_APPROVER";

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
        ApplicationRolesManager subject = poplar();

        assertEquals(ApplicationRolesManager.ADMIN,subject.getRoles().get(0).getName());
        assertEquals(Set.of(ApplicationRolesManager.ADMIN),subject.expand(Set.of(ApplicationRolesManager.ADMIN)));
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
                new ApplicationRolesManager(
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
                new ApplicationRolesManager(
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
                new ApplicationRolesManager(
                    List.of(new RoleDefinition("ROLE_A","A",Set.of())),
                    "ROLE_Other"));
    }

    @Test
    public void
    rejectsNonUpperSnakeName()
    {
        for (String name : List.of("ROLE_PromotionsReviewer","ROLE_promotions","ADMIN","ROLE_","ROLE_PROMOTIONS-REVIEWER"))
            assertThrows(
                IllegalArgumentException.class,
                () ->
                    new ApplicationRolesManager(
                        List.of(new RoleDefinition(name,"Bad",Set.of())),
                        name));
    }

    @Test
    public void
    rejectsImplyingAdmin()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRolesManager(
                    List.of(new RoleDefinition("ROLE_A","A",Set.of(ApplicationRolesManager.ADMIN))),
                    "ROLE_A"));
    }

    @Test
    public void
    rejectsApplicationRoleNamedAdmin()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRolesManager(
                    List.of(new RoleDefinition(ApplicationRolesManager.ADMIN,"Admin",Set.of())),
                    ApplicationRolesManager.ADMIN));
    }

    @Test
    public void
    rejectsDuplicateNames()
    {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ApplicationRolesManager(
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
                new ApplicationRolesManager(
                    List.of(new RoleDefinition("ROLE_A","A",Set.of())),
                    ApplicationRolesManager.ADMIN));
    }

    @Test
    public void
    expandTreatsNullAsEmpty()
    {
        assertEquals(Set.of(),poplar().expand(null));
    }

    @Test
    public void
    graniteSupplierYieldsAuditorAndUserCatalog()
    {
        Supplier<ApplicationRolesManager> supplier = new GraniteApplicationRolesManagerSupplier();

        assertInstanceOf(Supplier.class,supplier);
        ApplicationRolesManager subject = supplier.get();

        assertEquals(
            List.of("ROLE_ADMIN","ROLE_AUDITOR","ROLE_USER"),
            subject.getRoles().stream().map(RoleDefinition::getName).toList());
        assertEquals(List.of("ROLE_ADMIN","ROLE_AUDITOR","ROLE_USER"),subject.getRoleNames());
        assertEquals("ROLE_USER",subject.getDefaultRole());
    }

    private ApplicationRolesManager
    poplar()
    {
        return
            new ApplicationRolesManager(
                List.of(
                    new RoleDefinition(REVIEWER,"Reviewer",Set.of()),
                    new RoleDefinition(PLANNER,"Planner",Set.of(REVIEWER)),
                    new RoleDefinition(APPROVER,"Approver",Set.of(PLANNER))),
                REVIEWER);
    }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
