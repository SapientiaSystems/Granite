//////////////////////////////////////////////////////////////////////////////
// SessionRequestContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public
class SessionRequestContext
{
    public static final String ATTRIBUTE = "granite.session-request-context";

    private static final String       BEARER_PREFIX = "Bearer ";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String,String> cookies;
    private final Optional<Long>     sessionId;
    private final Optional<Long>     callerUserId;
    private final List<String>       setCookies;

    public
    SessionRequestContext(Map<String,String> cookies,String authorization)
    {
        JsonNode claims = claims(authorization);

        this.cookies = Map.copyOf(cookies);
        this.sessionId = number(claims,"jti");
        this.callerUserId = number(claims,"sub");
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

        SessionRequestContext context =
            new SessionRequestContext(cookies,request.getHeader(HttpHeaders.AUTHORIZATION));

        request.setAttribute(ATTRIBUTE,context);
        return context;
    }

    private static JsonNode
    claims(String authorization)
    {
        if (authorization == null || !authorization.regionMatches(true,0,BEARER_PREFIX,0,BEARER_PREFIX.length()))
            return null;

        String[] parts = authorization.substring(BEARER_PREFIX.length()).trim().split("\\.");

        if (parts.length < 2)
            return null;

        try
        {
            return MAPPER.readTree(Base64.getUrlDecoder().decode(parts[1]));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static Optional<Long>
    number(JsonNode claims,String name)
    {
        if (claims == null || !claims.isObject() || !claims.hasNonNull(name))
            return Optional.empty();

        try
        {
            return Optional.of(Long.valueOf(claims.get(name).asText()));
        }
        catch (NumberFormatException e)
        {
            return Optional.empty();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
