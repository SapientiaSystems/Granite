//////////////////////////////////////////////////////////////////////////////
// OktaEndpoints.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

public
class OktaEndpoints
{
    private final String issuer;
    private final String authorizationEndpoint;
    private final String tokenEndpoint;
    private final String jwksUri;

    public
    OktaEndpoints(
        String issuer,
        String authorizationEndpoint,
        String tokenEndpoint,
        String jwksUri)
    {
        this.issuer                = issuer;
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint         = tokenEndpoint;
        this.jwksUri               = jwksUri;
    }

    public String
    getIssuer() { return issuer; }

    public String
    getAuthorizationEndpoint() { return authorizationEndpoint; }

    public String
    getTokenEndpoint() { return tokenEndpoint; }

    public String
    getJwksUri() { return jwksUri; }
}

//////////////////////////////////////////////////////////////////////////////
