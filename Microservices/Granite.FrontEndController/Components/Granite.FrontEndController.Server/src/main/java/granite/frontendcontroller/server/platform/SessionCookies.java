//////////////////////////////////////////////////////////////////////////////
// SessionCookies.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.time.Instant;

public
class SessionCookies
{
    public static final String SESSION_COOKIE = "GRANITE_SID";
    public static final String DEVICE_COOKIE  = "GRANITE_DID";
    public static final String LOGIN_COOKIE   = "GRANITE_LOGIN";

    private static final Duration LOGIN_LIFETIME = Duration.ofMinutes(5);

    private
    SessionCookies() {}

    public static String
    issue(String handle,Instant expires,Instant now)
    {
        ResponseCookie.ResponseCookieBuilder builder = builder(handle);

        if (expires != null)
            builder.maxAge(Math.max(1L,Duration.between(now,expires).getSeconds()));

        return builder.build().toString();
    }

    public static String
    clear()
    {
        return builder("").maxAge(0).build().toString();
    }

    public static String
    issueLogin(String transactionId)
    {
        return loginBuilder(transactionId).maxAge(LOGIN_LIFETIME).build().toString();
    }

    public static String
    clearLogin()
    {
        return loginBuilder("").maxAge(0).build().toString();
    }

    private static ResponseCookie.ResponseCookieBuilder
    builder(String value)
    {
        return
            ResponseCookie
                .from(SESSION_COOKIE,value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/");
    }

    private static ResponseCookie.ResponseCookieBuilder
    loginBuilder(String value)
    {
        return
            ResponseCookie
                .from(LOGIN_COOKIE,value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/");
    }
}

//////////////////////////////////////////////////////////////////////////////
