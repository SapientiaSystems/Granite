//////////////////////////////////////////////////////////////////////////////
// SessionRequestContextTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
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
        SecurityContextHolder.clearContext();
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

    public static Jwt
    jwt(Long sessionId,Long userId)
    {
        Jwt.Builder builder = Jwt.withTokenValue("verified.token.value").header("alg","RS256");

        if (sessionId != null)
            builder.jti(String.valueOf(sessionId));

        if (userId != null)
            builder.subject(String.valueOf(userId));

        return builder.claim("verified",true).build();
    }

    public static JwtAuthenticationToken
    principal(Jwt jwt)
    {
        return new JwtAuthenticationToken(jwt,List.of(),jwt.getSubject());
    }

    @Test
    public void
    readsCookiesAndVerifiedTokenClaims()
    {
        SessionRequestContext context =
            new SessionRequestContext(Map.of("GRANITE_DID","device-1","other","x"),jwt(42L,7L));

        assertEquals("device-1",context.getCookie("GRANITE_DID").orElseThrow());
        assertTrue(context.getCookie("missing").isEmpty());
        assertEquals(42L,context.getSessionId().orElseThrow());
        assertEquals(7L,context.getCallerUserId().orElseThrow());
    }

    @Test
    public void
    absentOrNonNumericClaimsGiveNoIdentity()
    {
        SessionRequestContext none = new SessionRequestContext(Map.of(),null);
        SessionRequestContext text = new SessionRequestContext(Map.of(),Jwt.withTokenValue("t").header("alg","RS256").jti("x").subject("y").build());

        for (SessionRequestContext context : List.of(none,text))
        {
            assertTrue(context.getSessionId().isEmpty());
            assertTrue(context.getCallerUserId().isEmpty());
        }
    }

    @Test
    public void
    fromCurrentRequestCapturesAndRegistersItself()
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-input");

        request.setCookies(new Cookie("GRANITE_DID","device-9"),new Cookie("XSRF-TOKEN","t"));
        request.setUserPrincipal(principal(jwt(5L,6L)));
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
    anUnverifiedBearerHeaderIsNeverTrusted()
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-input");

        request.addHeader("Authorization",bearer("{\"jti\":\"5\",\"sub\":\"6\"}"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        SessionRequestContext context = SessionRequestContext.fromCurrentRequest();

        assertTrue(context.getSessionId().isEmpty());
        assertTrue(context.getCallerUserId().isEmpty());
    }

    @Test
    public void
    anAnonymousAuthenticationGivesNoIdentity()
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-input");

        request.setUserPrincipal(new AnonymousAuthenticationToken("key","anonymousUser",List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertTrue(SessionRequestContext.fromCurrentRequest().getCallerUserId().isEmpty());
    }

    @Test
    public void
    theCallerComesFromTheRequestPrincipal()
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST","/frontendcontroller-user-service/get-users-output");

        request.setUserPrincipal(principal(jwt(5L,6L)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        SessionRequestContext context = SessionRequestContext.fromCurrentRequest();

        assertEquals(5L,context.getSessionId().orElseThrow());
        assertEquals(6L,context.getCallerUserId().orElseThrow());
    }

    @Test
    public void
    theStaticSecurityContextIsNotRead()
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST","/frontendcontroller-user-service/get-users-output");

        SecurityContextHolder.getContext().setAuthentication(principal(jwt(5L,6L)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertTrue(SessionRequestContext.fromCurrentRequest().getCallerUserId().isEmpty());
    }

    @Test
    public void
    aTokenAuthenticationWhosePrincipalIsNotTheJwtUsesItsToken()
    {
        MockHttpServletRequest                      request   = new MockHttpServletRequest("POST","/frontendcontroller-user-service/get-users-output");
        AbstractOAuth2TokenAuthenticationToken<Jwt> principal =
            new AbstractOAuth2TokenAuthenticationToken<>(jwt(8L,9L),"user-9",null,List.of())
            {
                @Override
                public Map<String,Object>
                getTokenAttributes() { return getToken().getClaims(); }
            };

        principal.setAuthenticated(true);
        request.setUserPrincipal(principal);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        SessionRequestContext context = SessionRequestContext.fromCurrentRequest();

        assertEquals(8L,context.getSessionId().orElseThrow());
        assertEquals(9L,context.getCallerUserId().orElseThrow());
    }

    @Test
    public void
    anUnauthenticatedTokenPrincipalGivesNoIdentity()
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST","/frontendcontroller-user-service/get-users-output");

        request.setUserPrincipal(new JwtAuthenticationToken(jwt(5L,6L)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertTrue(SessionRequestContext.fromCurrentRequest().getCallerUserId().isEmpty());
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
