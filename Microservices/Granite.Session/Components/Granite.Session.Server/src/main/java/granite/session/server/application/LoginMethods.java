//////////////////////////////////////////////////////////////////////////////
// LoginMethods.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

public
class LoginMethods
{
    public static final String NONE_ENABLED =
        "at least one login method must be enabled: granite.session.login.password.enabled or granite.session.login.okta.enabled";

    private final boolean passwordEnabled;
    private final boolean oktaEnabled;

    public
    LoginMethods(boolean passwordEnabled,boolean oktaEnabled)
    {
        if (!passwordEnabled && !oktaEnabled)
            throw new IllegalStateException(NONE_ENABLED);

        this.passwordEnabled = passwordEnabled;
        this.oktaEnabled     = oktaEnabled;
    }

    public boolean
    isPasswordEnabled() { return passwordEnabled; }

    public boolean
    isOktaEnabled() { return oktaEnabled; }
}

//////////////////////////////////////////////////////////////////////////////
