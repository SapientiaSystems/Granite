//////////////////////////////////////////////////////////////////////////////
// SessionCookies.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.foundation.core.security.GraniteCookieNames;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public
class SessionCookies
{
    private static final Duration LOGIN_LIFETIME = Duration.ofMinutes(5);

    private final GraniteCookieNames names;

    public
    SessionCookies(GraniteCookieNames names)
    {
        this.names = Objects.requireNonNull(names,"names");
    }

    public String
    issue(String handle,Instant expires,Instant now)
    {
        ResponseCookie.ResponseCookieBuilder builder = builder(handle);

        if (expires != null)
            builder.maxAge(Math.max(1L,Duration.between(now,expires).getSeconds()));

        return builder.build().toString();
    }

    public String
    clear()
    {
        return builder("").maxAge(0).build().toString();
    }

    public String
    issueLogin(String transactionId)
    {
        return loginBuilder(transactionId).maxAge(LOGIN_LIFETIME).build().toString();
    }

    public String
    clearLogin()
    {
        return loginBuilder("").maxAge(0).build().toString();
    }

    public String
    getSessionName() { return names.getSession(); }

    public String
    getDeviceName() { return names.getDevice(); }

    public String
    getLoginName() { return names.getLogin(); }

    private ResponseCookie.ResponseCookieBuilder
    builder(String value)
    {
        return names.hostCookie(names.getSession(),value).httpOnly(true).sameSite("Strict");
    }

    private ResponseCookie.ResponseCookieBuilder
    loginBuilder(String value)
    {
        return names.hostCookie(names.getLogin(),value).httpOnly(true).sameSite("Lax");
    }
}

//////////////////////////////////////////////////////////////////////////////
