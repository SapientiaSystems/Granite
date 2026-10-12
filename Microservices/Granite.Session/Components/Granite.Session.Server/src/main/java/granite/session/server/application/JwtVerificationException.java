//////////////////////////////////////////////////////////////////////////////
// JwtVerificationException.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

public
class JwtVerificationException
    extends RuntimeException
{
    public
    JwtVerificationException(String reason)
    {
        super(reason);
    }

    public
    JwtVerificationException(String reason,Throwable cause)
    {
        super(reason,cause);
    }
}

//////////////////////////////////////////////////////////////////////////////
