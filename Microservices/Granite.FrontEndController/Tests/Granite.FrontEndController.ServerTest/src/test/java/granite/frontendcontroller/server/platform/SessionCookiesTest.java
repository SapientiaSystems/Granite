//////////////////////////////////////////////////////////////////////////////
// SessionCookiesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.foundation.core.security.GraniteCookieNames;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
public
class SessionCookiesTest
{
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    public void
    prefixedNamesAndHostRules()
    {
        SessionCookies cookies = new SessionCookies(new GraniteCookieNames("POPLAR"));

        String session = cookies.issue("h",NOW.plusSeconds(60),NOW);
        String login   = cookies.issueLogin("t");

        assertTrue(session.startsWith("__Host-POPLAR_SID=h;"),session);
        assertTrue(session.contains("Path=/"));
        assertTrue(session.contains("Secure"));
        assertTrue(session.contains("HttpOnly"));
        assertTrue(session.contains("SameSite=Strict"));
        assertFalse(session.contains("Domain"));

        assertTrue(login.startsWith("__Host-POPLAR_LOGIN=t;"),login);
        assertTrue(login.contains("Path=/"));
        assertTrue(login.contains("Secure"));
        assertTrue(login.contains("HttpOnly"));
        assertTrue(login.contains("SameSite=Lax"));
        assertFalse(login.contains("Domain"));

        assertTrue(cookies.clear().startsWith("__Host-POPLAR_SID=;"));
        assertTrue(cookies.clearLogin().startsWith("__Host-POPLAR_LOGIN=;"));
        assertEquals("__Host-POPLAR_SID",cookies.getSessionName());
        assertEquals("__Host-POPLAR_DID",cookies.getDeviceName());
        assertEquals("__Host-POPLAR_LOGIN",cookies.getLoginName());
    }
}

//////////////////////////////////////////////////////////////////////////////
