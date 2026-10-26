//////////////////////////////////////////////////////////////////////////////
// SessionTestSupport.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.server.platform.SessionRequestContextTest;
import granite.frontendcontroller.service.requestreply.SessionInput;

import java.util.Map;

public
class SessionTestSupport
{
    public static final String HANDLE   = "handle-secret-value";
    public static final String PASSWORD = "password-secret-value";

    private
    SessionTestSupport() {}

    public static SessionRequestContext
    context(String deviceId,Long sessionId,Long userId)
    {
        Map<String,String> cookies = deviceId == null ? Map.of() : Map.of("GRANITE_DID",deviceId);
        String             bearer =
            sessionId == null
                ? null
                : SessionRequestContextTest.bearer("{\"jti\":\"" + sessionId + "\",\"sub\":\"" + userId + "\"}");

        return new SessionRequestContext(cookies,bearer);
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
