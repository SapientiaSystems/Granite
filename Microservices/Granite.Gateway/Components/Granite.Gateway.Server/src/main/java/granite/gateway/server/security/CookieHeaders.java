//////////////////////////////////////////////////////////////////////////////
// CookieHeaders.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;

public
class CookieHeaders
{
    private
    CookieHeaders() {}

    public static void
    strip(HttpHeaders headers,String name)
    {
        List<String> kept = new ArrayList<>();

        for (String pair : pairs(headers))
            if (!nameOf(pair).equals(name))
                kept.add(pair);

        write(headers,kept);
    }

    public static void
    append(HttpHeaders headers,String name,String value)
    {
        List<String> pairs = pairs(headers);

        pairs.add(name + "=" + value);
        write(headers,pairs);
    }

    private static List<String>
    pairs(HttpHeaders headers)
    {
        List<String> pairs  = new ArrayList<>();
        List<String> values = headers.get(HttpHeaders.COOKIE);

        if (values == null)
            return pairs;

        for (String value : values)
            for (String part : value.split(";"))
                if (!part.isBlank())
                    pairs.add(part.trim());

        return pairs;
    }

    private static String
    nameOf(String pair)
    {
        int separator = pair.indexOf('=');

        return separator < 0 ? pair.trim() : pair.substring(0,separator).trim();
    }

    private static void
    write(HttpHeaders headers,List<String> pairs)
    {
        if (pairs.isEmpty())
            headers.remove(HttpHeaders.COOKIE);
        else
            headers.set(HttpHeaders.COOKIE,String.join("; ",pairs));
    }
}

//////////////////////////////////////////////////////////////////////////////
