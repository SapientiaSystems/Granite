//////////////////////////////////////////////////////////////////////////////
// ClientTokenResult.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

public
class ClientTokenResult
{
    private final ClientTokenOutcome outcome;
    private final String             token;
    private final long               expiresInSeconds;

    private
    ClientTokenResult(ClientTokenOutcome outcome,String token,long expiresInSeconds)
    {
        this.outcome          = outcome;
        this.token            = token;
        this.expiresInSeconds = expiresInSeconds;
    }

    public static ClientTokenResult
    ok(String token,long expiresInSeconds)
    {
        return new ClientTokenResult(ClientTokenOutcome.OK,token,expiresInSeconds);
    }

    public static ClientTokenResult
    invalidClient()
    {
        return new ClientTokenResult(ClientTokenOutcome.INVALID_CLIENT,null,0L);
    }

    public static ClientTokenResult
    unavailable()
    {
        return new ClientTokenResult(ClientTokenOutcome.UNAVAILABLE,null,0L);
    }

    public ClientTokenOutcome
    getOutcome() { return outcome; }

    public String
    getToken() { return token; }

    public long
    getExpiresInSeconds() { return expiresInSeconds; }

    @Override
    public String
    toString()
    {
        return "ClientTokenResult{outcome=" + outcome + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
