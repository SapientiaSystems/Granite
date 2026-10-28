//////////////////////////////////////////////////////////////////////////////
// SessionRequestContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public
class SessionRequestContext
{
    public static final String ATTRIBUTE = "granite.session-request-context";

    private final Map<String,String> cookies;
    private final Optional<Long>     sessionId;
    private final Optional<Long>     callerUserId;
    private final List<String>       setCookies;

    public
    SessionRequestContext(Map<String,String> cookies,Jwt verifiedToken)
    {
        this.cookies = Map.copyOf(cookies);
        this.sessionId = number(verifiedToken == null ? null : verifiedToken.getId());
        this.callerUserId = number(verifiedToken == null ? null : verifiedToken.getSubject());
        this.setCookies = new CopyOnWriteArrayList<>();
    }

    public Optional<String>
    getCookie(String name) { return Optional.ofNullable(cookies.get(name)); }

    public Optional<Long>
    getSessionId() { return sessionId; }

    public Optional<Long>
    getCallerUserId() { return callerUserId; }

    public void
    addSetCookie(String headerValue) { setCookies.add(headerValue); }

    public List<String>
    getSetCookies() { return List.copyOf(setCookies); }

    public static SessionRequestContext
    fromCurrentRequest()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (!(attributes instanceof ServletRequestAttributes servlet))
            return new SessionRequestContext(Map.of(),null);

        HttpServletRequest request = servlet.getRequest();

        if (request.getAttribute(ATTRIBUTE) instanceof SessionRequestContext existing)
            return existing;

        Map<String,String> cookies = new HashMap<>();

        if (request.getCookies() != null)
        {
            for (Cookie cookie : request.getCookies())
                cookies.putIfAbsent(cookie.getName(),cookie.getValue());
        }

        SessionRequestContext context = new SessionRequestContext(cookies,verifiedToken(request));

        request.setAttribute(ATTRIBUTE,context);
        return context;
    }

    private static Jwt
    verifiedToken(HttpServletRequest request)
    {
        if (!(request.getUserPrincipal() instanceof Authentication authentication)
            || !authentication.isAuthenticated())
            return null;

        if (authentication.getPrincipal() instanceof Jwt jwt)
            return jwt;

        if (authentication instanceof AbstractOAuth2TokenAuthenticationToken<?> token
            && token.getToken() instanceof Jwt jwt)
            return jwt;

        return null;
    }

    private static Optional<Long>
    number(String value)
    {
        if (value == null)
            return Optional.empty();

        try
        {
            return Optional.of(Long.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return Optional.empty();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
