//////////////////////////////////////////////////////////////////////////////
// UserTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    testUserDataJsonExcludesPassword() throws Exception
    {
        UserData data =
            new UserData()
                .setUserId(1L)
                .setUserName("alice@example.com")
                .setExternalIdentity("google:abc")
                .setFoo("something");
        String json = new ObjectMapper().writeValueAsString(data);

        assertFalse(
            json.toLowerCase().contains("password"),
            "UserData JSON must never contain a password field: " + json);
    }
}

//////////////////////////////////////////////////////////////////////////////
