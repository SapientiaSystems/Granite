//////////////////////////////////////////////////////////////////////////////
// Oauth2ValidationException.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

public
class Oauth2ValidationException
    extends RuntimeException
{
    public
    Oauth2ValidationException(String reason)
    {
        super(reason);
    }

    public
    Oauth2ValidationException(String reason,Throwable cause)
    {
        super(reason,cause);
    }
}

//////////////////////////////////////////////////////////////////////////////
