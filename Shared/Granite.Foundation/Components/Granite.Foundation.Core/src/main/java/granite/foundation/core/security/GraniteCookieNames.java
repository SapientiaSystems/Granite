//////////////////////////////////////////////////////////////////////////////
// GraniteCookieNames.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.springframework.core.env.PropertyResolver;
import org.springframework.http.ResponseCookie;

import java.util.Objects;
import java.util.regex.Pattern;

public
final class GraniteCookieNames
{
    public static final String  PREFIX         = "granite.cookies.prefix";
    public static final String  DEFAULT_PREFIX = "GRANITE";
    private static final Pattern VALID_PREFIX  = Pattern.compile("[A-Z0-9_]+");
    private static final String HOST_PREFIX    = "__Host-";

    private final String session;
    private final String device;
    private final String login;
    private final String csrf;

    public
    GraniteCookieNames(String prefix)
    {
        Objects.requireNonNull(prefix,"prefix");

        if (!VALID_PREFIX.matcher(prefix).matches())
            throw new IllegalArgumentException(PREFIX + " must match [A-Z0-9_]+");

        this.session = HOST_PREFIX + prefix + "_SID";
        this.device  = HOST_PREFIX + prefix + "_DID";
        this.login   = HOST_PREFIX + prefix + "_LOGIN";
        this.csrf    = HOST_PREFIX + prefix + "-XSRF-TOKEN";
    }

    public static GraniteCookieNames
    from(PropertyResolver properties)
    {
        return new GraniteCookieNames(properties.getProperty(PREFIX,DEFAULT_PREFIX));
    }

    public String
    getSession() { return session; }

    public String
    getDevice() { return device; }

    public String
    getLogin() { return login; }

    public String
    getCsrf() { return csrf; }

    public ResponseCookie.ResponseCookieBuilder
    hostCookie(String name,String value)
    {
        return ResponseCookie.from(name,value).secure(true).path("/");
    }
}

//////////////////////////////////////////////////////////////////////////////
