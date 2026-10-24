//////////////////////////////////////////////////////////////////////////////
// IOktaClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.util.Optional;

public
interface IOktaClient
{
    OktaEndpoints
    getEndpoints();

    String
    buildAuthorizeUrl(String state,String nonce,String codeChallenge,boolean promptLogin);

    OktaIdentity
    exchange(String code,String codeVerifier,String nonce);

    Optional<String>
    buildEndSessionUrl(String idToken);
}

//////////////////////////////////////////////////////////////////////////////
