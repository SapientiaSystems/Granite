//////////////////////////////////////////////////////////////////////////////
// SessionLoginSettings.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import strata.foundation.core.configuration.IConfiguration;

public
class SessionLoginSettings
{
    public static final String PASSWORD_ENABLED = "granite.session.login.password.enabled";
    public static final String OKTA_ENABLED     = "granite.session.login.okta.enabled";
    public static final String NONE_ENABLED     =
        "at least one login method must be enabled: " + PASSWORD_ENABLED + " or " + OKTA_ENABLED;

    private final boolean passwordEnabled;
    private final boolean oktaEnabled;

    public
    SessionLoginSettings(boolean passwordEnabled,boolean oktaEnabled)
    {
        if (!passwordEnabled && !oktaEnabled)
            throw new IllegalStateException(NONE_ENABLED);

        this.passwordEnabled = passwordEnabled;
        this.oktaEnabled = oktaEnabled;
    }

    public boolean
    isPasswordEnabled() { return passwordEnabled; }

    public boolean
    isOktaEnabled() { return oktaEnabled; }

    public static SessionLoginSettings
    fromConfiguration(IConfiguration configuration)
    {
        return
            new SessionLoginSettings(
                Boolean.parseBoolean(configuration.getProperty(PASSWORD_ENABLED,"true")),
                Boolean.parseBoolean(configuration.getProperty(OKTA_ENABLED,"false")));
    }
}

//////////////////////////////////////////////////////////////////////////////
