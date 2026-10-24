//////////////////////////////////////////////////////////////////////////////
// SessionRequestContextTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionRequestContextTest
{
    private static final Instant NOW = Instant.parse("2026-10-24T12:00:00Z");

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    public static String
    bearer(String claims)
    {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        return
            "Bearer "
                + encoder.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8))
                + "."
                + encoder.encodeToString(claims.getBytes(StandardCharsets.UTF_8))
                + ".signature";
    }

    @Test
    public void
    readsCookiesAndBearerClaims()
    {
        SessionRequestContext context =
            new SessionRequestContext(
                Map.of("GRANITE_DID","device-1","other","x"),
                bearer("{\"jti\":\"42\",\"sub\":\"7\"}"));

        assertEquals("device-1",context.getCookie("GRANITE_DID").orElseThrow());
        assertTrue(context.getCookie("missing").isEmpty());
        assertEquals(42L,context.getSessionId().orElseThrow());
        assertEquals(7L,context.getCallerUserId().orElseThrow());
    }

    @Test
    public void
    malformedOrAbsentBearerGivesNoIdentity()
    {
        String[] bad = {null,"","Basic abc","Bearer ","Bearer a.b.c","Bearer onlyone",bearer("{\"jti\":\"x\"}"),bearer("[]")};

        for (String authorization : bad)
        {
            SessionRequestContext context = new SessionRequestContext(Map.of(),authorization);

            assertTrue(context.getSessionId().isEmpty(),String.valueOf(authorization));
            assertTrue(context.getCallerUserId().isEmpty(),String.valueOf(authorization));
        }
    }

    @Test
    public void
    fromCurrentRequestCapturesAndRegistersItself()
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-input");

        request.setCookies(new Cookie("GRANITE_DID","device-9"),new Cookie("XSRF-TOKEN","t"));
        request.addHeader("Authorization",bearer("{\"jti\":5,\"sub\":6}"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        SessionRequestContext context = SessionRequestContext.fromCurrentRequest();

        assertEquals("device-9",context.getCookie("GRANITE_DID").orElseThrow());
        assertEquals(5L,context.getSessionId().orElseThrow());
        assertEquals(6L,context.getCallerUserId().orElseThrow());
        assertSame(context,request.getAttribute(SessionRequestContext.ATTRIBUTE));
        assertSame(context,SessionRequestContext.fromCurrentRequest());
    }

    @Test
    public void
    outsideARequestTheContextIsEmpty()
    {
        SessionRequestContext context = SessionRequestContext.fromCurrentRequest();

        assertTrue(context.getCookie("GRANITE_DID").isEmpty());
        assertTrue(context.getSessionId().isEmpty());
        assertTrue(context.getSetCookies().isEmpty());
    }

    @Test
    public void
    issuedSessionCookieHasAllAttributes()
    {
        String header = SessionCookies.issue("the-handle",NOW.plusSeconds(3600),NOW);

        assertTrue(header.startsWith("GRANITE_SID=the-handle;"));
        assertTrue(header.contains("Max-Age=3600"));
        assertTrue(header.contains("Path=/"));
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.contains("Secure"));
        assertTrue(header.contains("SameSite=Strict"));
    }

    @Test
    public void
    issuedCookieWithoutExpiryIsASessionCookieAndMaxAgeHasAMinimum()
    {
        assertFalse(SessionCookies.issue("h",null,NOW).contains("Max-Age"));
        assertTrue(SessionCookies.issue("h",NOW.minusSeconds(5),NOW).contains("Max-Age=1"));
    }

    @Test
    public void
    clearedCookieExpiresImmediately()
    {
        String header = SessionCookies.clear();

        assertTrue(header.startsWith("GRANITE_SID=;"));
        assertTrue(header.contains("Max-Age=0"));
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.contains("Secure"));
        assertTrue(header.contains("SameSite=Strict"));
        assertTrue(header.contains("Path=/"));
    }
}

//////////////////////////////////////////////////////////////////////////////
