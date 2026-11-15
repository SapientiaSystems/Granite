//////////////////////////////////////////////////////////////////////////////
// IOktaClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

public
interface IOktaClient
{
    OktaEndpoints
    getEndpoints();

    String
    buildAuthorizeUrl(String state,String nonce,String codeChallenge,boolean promptLogin);

    String
    buildAuthorizeUrl(String state,String nonce,String codeChallenge,boolean promptLogin,String redirectUri);

    OktaIdentity
    exchange(String code,String codeVerifier,String nonce);

    OktaIdentity
    exchange(String code,String codeVerifier,String nonce,String redirectUri);
}

//////////////////////////////////////////////////////////////////////////////
