//////////////////////////////////////////////////////////////////////////////
// PasswordLoginSettings.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

public
class PasswordLoginSettings
{
    private boolean enabled;

    public
    PasswordLoginSettings()
    {
        enabled = true;
    }

    public PasswordLoginSettings
    setEnabled(boolean value)
    {
        enabled = value;
        return this;
    }

    public boolean
    isEnabled() { return enabled; }
}

//////////////////////////////////////////////////////////////////////////////
