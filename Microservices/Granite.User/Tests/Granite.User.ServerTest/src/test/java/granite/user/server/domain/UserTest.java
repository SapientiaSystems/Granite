//////////////////////////////////////////////////////////////////////////////
// UserTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class UserTest
{
    @Test
    public void
    testUserNameDefaultsToNull()
    {
        assertNull(new User().getUserName());
    }

    @Test
    public void
    testSetUserNameFluentlyAndRead()
    {
        User target = new User();

        assertEquals(target,target.setUserName("alice@example.com"));
        assertEquals("alice@example.com",target.getUserName());
    }

    @Test
    public void
    testPasswordDefaultsToNull()
    {
        assertNull(new User().getPassword());
    }

    @Test
    public void
    testSetPasswordFluentlyAndRead()
    {
        User target = new User();

        assertEquals(target,target.setPassword("hash"));
        assertEquals("hash",target.getPassword());
    }

    @Test
    public void
    testExternalIdentityDefaultsToNull()
    {
        assertNull(new User().getExternalIdentity());
    }

    @Test
    public void
    testSetExternalIdentityFluentlyAndRead()
    {
        User target = new User();

        assertEquals(target,target.setExternalIdentity("google:abc"));
        assertEquals("google:abc",target.getExternalIdentity());
    }

    @Test
    public void
    testAuthoritiesDefaultsToEmptySet()
    {
        Set<String> authorities = new User().getAuthorities();

        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    public void
    testSetAuthoritiesFluentlyAndRead()
    {
        User        target = new User();
        Set<String> values = Set.of("ADMIN","USER");

        assertEquals(target,target.setAuthorities(values));
        assertEquals(values,target.getAuthorities());
    }

    @Test
    public void
    testValuesDefaultsToEmptyMap()
    {
        User target = new User();

        assertNotNull(target.getValues());
        assertTrue(target.getValues().isEmpty());
    }

    @Test
    public void
    testSetValueFluentlyAndReadByType()
    {
        User target = new User();

        assertSame(target,target.setValue("preferredTheme","dark"));
        assertEquals(Optional.of("dark"),target.getValue(String.class,"preferredTheme"));
    }

    @Test
    public void
    testGetValueOnMissingKeyReturnsEmpty()
    {
        assertTrue(new User().getValue(String.class,"missing").isEmpty());
    }

    @Test
    public void
    testGetValueOnTypeMismatchReturnsEmpty()
    {
        User target = new User().setValue("count","not-a-number");

        assertTrue(target.getValue(Integer.class,"count").isEmpty());
    }

    @Test
    public void
    testHasValueReturnsTrueForPresentKey()
    {
        assertTrue(new User().setValue("preferredTheme","dark").hasValue("preferredTheme"));
    }

    @Test
    public void
    testHasValueReturnsFalseForAbsentKey()
    {
        assertFalse(new User().hasValue("missing"));
    }

    @Test
    public void
    testUserDataJsonCarriesOnlyMaskedPassword() throws Exception
    {
        UserData data =
            new UserData()
                .setUserId(1L)
                .setUserName("alice@example.com")
                .setExternalIdentity("google:abc")
                .setAuthorities(Set.of("ROLE_User"))
                .setPassword("********");
        String json = new ObjectMapper().writeValueAsString(data);

        assertTrue(json.contains("\"password\":\"********\""),json);
        assertFalse(
            json.contains("$2"),
            "UserData JSON must never contain a password hash: " + json);
    }
}

//////////////////////////////////////////////////////////////////////////////
