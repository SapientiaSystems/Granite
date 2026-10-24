//////////////////////////////////////////////////////////////////////////////
// SessionTestSupport.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.server.platform.SessionRequestContextTest;
import granite.frontendcontroller.service.requestreply.LoginInput;
import granite.frontendcontroller.service.requestreply.LogoutInput;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

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

    public static HandleInputRequest
    login(String userName,String password)
    {
        return
            new HandleInputRequest()
                .setInput(
                    new LoginInput()
                        .setUserName(userName)
                        .setPassword(password)
                        .setSource("Session.New")
                        .setCommand("Login")
                        .setDestination("LoginView"));
    }

    public static HandleInputRequest
    logout()
    {
        return
            new HandleInputRequest()
                .setInput(
                    new LogoutInput()
                        .setSource("Session.Current")
                        .setCommand("Logout")
                        .setDestination("LoginView"));
    }
}

//////////////////////////////////////////////////////////////////////////////
