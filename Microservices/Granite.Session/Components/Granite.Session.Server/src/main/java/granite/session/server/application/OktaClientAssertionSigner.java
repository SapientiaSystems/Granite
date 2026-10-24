//////////////////////////////////////////////////////////////////////////////
// OktaClientAssertionSigner.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.interfaces.RSAPrivateKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public
class OktaClientAssertionSigner
{
    private static final Duration LIFETIME = Duration.ofMinutes(5);

    private final RSAPrivateKey privateKey;
    private final String        keyId;
    private final Clock         clock;

    public
    OktaClientAssertionSigner(RSAPrivateKey privateKey,String keyId,Clock clock)
    {
        this.privateKey = Objects.requireNonNull(privateKey,"privateKey");
        this.keyId      = keyId;
        this.clock      = Objects.requireNonNull(clock,"clock");
    }

    public String
    sign(String clientId,String audience)
    {
        Instant      now    = clock.instant();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer(clientId)
                .subject(clientId)
                .audience(audience)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(LIFETIME)))
                .jwtID(UUID.randomUUID().toString())
                .build();

        try
        {
            SignedJWT assertion =
                new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build(),claims);

            assertion.sign(new RSASSASigner(privateKey));
            return assertion.serialize();
        }
        catch (JOSEException e)
        {
            throw new OktaLoginException("unable to sign the client assertion",e);
        }
    }

    public String
    getKeyId() { return keyId; }
}

//////////////////////////////////////////////////////////////////////////////
