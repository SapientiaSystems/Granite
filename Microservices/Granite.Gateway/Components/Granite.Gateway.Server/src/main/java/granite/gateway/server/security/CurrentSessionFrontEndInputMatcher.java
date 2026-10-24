//////////////////////////////////////////////////////////////////////////////
// CurrentSessionFrontEndInputMatcher.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import java.util.Set;

public
class CurrentSessionFrontEndInputMatcher
    extends FrontEndInputKeyMatcher
{
    public static final Set<String> CURRENT_SESSION_OUTPUT_KEYS = Set.of("Session.Current->SessionView");
    public static final Set<String> CURRENT_SESSION_INPUT_KEYS  = Set.of("Session.Current->Logout->LoginView");

    public
    CurrentSessionFrontEndInputMatcher()
    {
        super(CURRENT_SESSION_OUTPUT_KEYS,CURRENT_SESSION_INPUT_KEYS);
    }
}

//////////////////////////////////////////////////////////////////////////////
