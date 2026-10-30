//////////////////////////////////////////////////////////////////////////////
// SessionTestSupport.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.foundation.core.security.GraniteCookieNames;
import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.server.platform.SessionRequestContextTest;
import granite.frontendcontroller.service.requestreply.SessionInput;

import java.util.Map;

public
class SessionTestSupport
{
    public static final String HANDLE   = "handle-secret-value";
    public static final String PASSWORD = "password-secret-value";

    public static final GraniteCookieNames NAMES   = new GraniteCookieNames(GraniteCookieNames.DEFAULT_PREFIX);
    public static final SessionCookies     COOKIES = new SessionCookies(NAMES);

    private
    SessionTestSupport() {}

    public static SessionRequestContext
    context(String deviceId,Long sessionId,Long userId)
    {
        Map<String,String> cookies = deviceId == null ? Map.of() : Map.of(SessionTestSupport.NAMES.getDevice(),deviceId);

        return new SessionRequestContext(cookies,sessionId == null ? null : SessionRequestContextTest.jwt(sessionId,userId));
    }

    public static SessionInput
    login(String userName,String password)
    {
        return
            new SessionInput()
                .setUserName(userName)
                .setPassword(password)
                .setSource("Session.New")
                .setCommand("Login")
                .setDestination("LoginView");
    }

    public static SessionInput
    logout()
    {
        return
            new SessionInput()
                .setSource("Session.Current")
                .setCommand("Logout")
                .setDestination("LoginView");
    }
}

//////////////////////////////////////////////////////////////////////////////
