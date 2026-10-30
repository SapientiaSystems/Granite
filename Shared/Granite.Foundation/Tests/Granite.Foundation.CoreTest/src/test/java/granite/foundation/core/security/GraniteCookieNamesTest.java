//////////////////////////////////////////////////////////////////////////////
// GraniteCookieNamesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteCookieNamesTest
{
    @Test
    public void
    defaultNames()
    {
        var names = new GraniteCookieNames(GraniteCookieNames.DEFAULT_PREFIX);

        assertEquals("__Host-GRANITE_SID",names.getSession());
        assertEquals("__Host-GRANITE_DID",names.getDevice());
        assertEquals("__Host-GRANITE_LOGIN",names.getLogin());
        assertEquals("__Host-GRANITE-XSRF-TOKEN",names.getCsrf());
    }

    @Test
    public void
    customPrefix()
    {
        var names = new GraniteCookieNames("POPLAR");

        assertEquals("__Host-POPLAR_SID",names.getSession());
        assertEquals("__Host-POPLAR_DID",names.getDevice());
        assertEquals("__Host-POPLAR_LOGIN",names.getLogin());
        assertEquals("__Host-POPLAR-XSRF-TOKEN",names.getCsrf());
    }

    @Test
    public void
    rejectsInvalidPrefix()
    {
        assertThrows(IllegalArgumentException.class,() -> new GraniteCookieNames("poplar"));
        assertThrows(IllegalArgumentException.class,() -> new GraniteCookieNames("PO-PLAR"));
        assertThrows(IllegalArgumentException.class,() -> new GraniteCookieNames(" "));
        assertThrows(IllegalArgumentException.class,() -> new GraniteCookieNames(""));
    }

    @Test
    public void
    fromProperties()
    {
        var custom = new MockEnvironment().withProperty(GraniteCookieNames.PREFIX,"POPLAR");

        assertEquals("__Host-POPLAR_SID",GraniteCookieNames.from(custom).getSession());
        assertEquals("__Host-GRANITE_SID",GraniteCookieNames.from(new MockEnvironment()).getSession());
    }

    @Test
    public void
    everyCookieMeetsHostPrefixRules()
    {
        var names = new GraniteCookieNames("POPLAR");

        for (var name : List.of(
                names.getSession(),names.getDevice(),names.getLogin(),names.getCsrf()))
        {
            var cookie = names.hostCookie(name,"v").build().toString();

            assertTrue(cookie.contains("Secure"));
            assertTrue(cookie.contains("Path=/"));
            assertFalse(cookie.contains("Domain="));
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
