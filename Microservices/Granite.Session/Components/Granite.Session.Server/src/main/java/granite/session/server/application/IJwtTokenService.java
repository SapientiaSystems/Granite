//////////////////////////////////////////////////////////////////////////////
// IJwtTokenService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

public
interface IJwtTokenService
{
    String
    mint(Session session);

    String
    mintServiceToken(String subject,Collection<String> authorities,Duration lifetime);

    JwtVerificationResult
    verifySignatureAndExp(String jwt) throws JwtVerificationException;

    Instant
    getExpiration(String jwt);
}

//////////////////////////////////////////////////////////////////////////////
