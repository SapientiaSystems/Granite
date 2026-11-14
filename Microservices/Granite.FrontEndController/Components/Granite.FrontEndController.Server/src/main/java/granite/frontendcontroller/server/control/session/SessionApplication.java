//////////////////////////////////////////////////////////////////////////////
// SessionApplication.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

public
class SessionApplication
{
    public static final String DEFAULT = "granite";

    private
    SessionApplication() {}

    public static String
    resolve(String application)
    {
        return application == null || application.isBlank() ? DEFAULT : application.trim();
    }
}

//////////////////////////////////////////////////////////////////////////////
