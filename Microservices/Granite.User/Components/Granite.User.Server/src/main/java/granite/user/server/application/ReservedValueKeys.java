//////////////////////////////////////////////////////////////////////////////
// ReservedValueKeys.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import java.util.List;

public
class ReservedValueKeys
{
    private static final List<String> PREFIXES = List.of("preferences.","granite.");

    private
    ReservedValueKeys() {}

    public static boolean
    isReserved(String key)
    {
        for (String prefix : PREFIXES)
            if (key.startsWith(prefix))
                return true;

        return false;
    }
}

//////////////////////////////////////////////////////////////////////////////
