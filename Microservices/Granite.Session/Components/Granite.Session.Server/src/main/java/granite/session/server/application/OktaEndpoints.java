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
    private final String endSessionEndpoint;

    public
    OktaEndpoints(
        String issuer,
        String authorizationEndpoint,
        String tokenEndpoint,
        String jwksUri,
        String endSessionEndpoint)
    {
        this.issuer                = issuer;
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint         = tokenEndpoint;
        this.jwksUri               = jwksUri;
        this.endSessionEndpoint    = endSessionEndpoint;
    }

    public String
    getIssuer() { return issuer; }

    public String
    getAuthorizationEndpoint() { return authorizationEndpoint; }

    public String
    getTokenEndpoint() { return tokenEndpoint; }

    public String
    getJwksUri() { return jwksUri; }

    public String
    getEndSessionEndpoint() { return endSessionEndpoint; }
}

//////////////////////////////////////////////////////////////////////////////
