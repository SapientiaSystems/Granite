//////////////////////////////////////////////////////////////////////////////
// PublicFrontEndInputMatcher.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import java.util.Set;

public
class PublicFrontEndInputMatcher
    extends FrontEndInputKeyMatcher
{
    public static final Set<String> PUBLIC_OUTPUT_KEYS = Set.of("Session.New->LoginView");
    public static final Set<String> PUBLIC_INPUT_KEYS  =
        Set.of(
            "Session.New->Login->LoginView",
            "Session.New->StartOktaLogin->LoginView",
            "Session.New->CompleteOktaLogin->LoginView");

    public
    PublicFrontEndInputMatcher()
    {
        super(PUBLIC_OUTPUT_KEYS,PUBLIC_INPUT_KEYS);
    }
}

//////////////////////////////////////////////////////////////////////////////
