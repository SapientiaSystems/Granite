//////////////////////////////////////////////////////////////////////////////
// ClientIdGenerator.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import java.util.regex.Pattern;

public
class ClientIdGenerator
{
    public static final String  PREFIX_PROPERTY = "granite.client.id-prefix";
    public static final String  DEFAULT_PREFIX  = ClientCredentials.PREFIX;
    public static final Pattern PREFIX_PATTERN  = Pattern.compile("[a-z][a-z0-9]{0,7}_");

    private final String prefix;

    public
    ClientIdGenerator(String prefix)
    {
        if (prefix == null || !PREFIX_PATTERN.matcher(prefix).matches())
            throw
                new IllegalArgumentException(
                    PREFIX_PROPERTY + " must be a lowercase letter, up to 7 lowercase letters or digits, then _ (for example gc_)");

        this.prefix = prefix;
    }

    public String
    getPrefix() { return prefix; }

    public String
    newClientId()
    {
        return ClientCredentials.newClientId(prefix);
    }
}

//////////////////////////////////////////////////////////////////////////////
