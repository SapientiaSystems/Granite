//////////////////////////////////////////////////////////////////////////////
// OktaAuthoritiesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
public
class OktaAuthoritiesTest
{
    private static final Set<String>        USER_ROLES = Set.of("ROLE_USER","ROLE_AUDITOR");
    private static final Set<String>        GROUPS     = Set.of("Granite Admins","Everyone","Unmapped");
    private static final Map<String,String> MAPPING    =
        Map.of("Granite Admins","ROLE_ADMIN","Everyone","ROLE_USER");

    @Test
    public void
    userSourceUsesOnlyTheUsersAuthorities()
    {
        assertEquals(USER_ROLES,subject(OktaAuthoritiesSource.USER).calculate(USER_ROLES,GROUPS));
    }

    @Test
    public void
    groupsSourceUsesOnlyMappedGroups()
    {
        assertEquals(
            Set.of("ROLE_ADMIN","ROLE_USER"),
            subject(OktaAuthoritiesSource.OKTA_GROUPS).calculate(USER_ROLES,GROUPS));
    }

    @Test
    public void
    bothSourceIsTheUnion()
    {
        assertEquals(
            Set.of("ROLE_ADMIN","ROLE_USER","ROLE_AUDITOR"),
            subject(OktaAuthoritiesSource.BOTH).calculate(USER_ROLES,GROUPS));
    }

    @Test
    public void
    nullInputsGiveEmptyAuthorities()
    {
        assertEquals(Set.of(),subject(OktaAuthoritiesSource.BOTH).calculate(null,null));
    }

    @Test
    public void
    defaultSourceIsUser()
    {
        assertEquals(OktaAuthoritiesSource.USER,new OktaSettings().getAuthoritiesSource());
    }

    private OktaAuthorities
    subject(OktaAuthoritiesSource source)
    {
        return
            new OktaAuthorities(
                new OktaSettings()
                    .setAuthoritiesSource(source)
                    .setGroupMapping(MAPPING));
    }
}

//////////////////////////////////////////////////////////////////////////////
