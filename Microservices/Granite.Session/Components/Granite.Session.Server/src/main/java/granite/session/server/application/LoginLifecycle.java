//////////////////////////////////////////////////////////////////////////////
// LoginLifecycle.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;

public
class LoginLifecycle
{
    private Session source;
    private boolean replacing;

    public
    LoginLifecycle()
    {
        source    = null;
        replacing = false;
    }

    public LoginLifecycle
    setSource(Session value)
    {
        source = value;
        return this;
    }

    public LoginLifecycle
    setReplacing(boolean value)
    {
        replacing = value;
        return this;
    }

    public Session
    getSource() { return source; }

    public boolean
    isReplacing() { return replacing; }
}

//////////////////////////////////////////////////////////////////////////////
