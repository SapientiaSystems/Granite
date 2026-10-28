//////////////////////////////////////////////////////////////////////////////
// TestJwks.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public
class TestJwks
{
    private static final KeyPair TRUSTED   = generate();
    private static final KeyPair UNTRUSTED = generate();
    private static final RSAKey  JWK       = publicKey();

    private
    TestJwks() {}

    public static String
    jwkSetJson() { return new JWKSet(JWK).toPublicJWKSet().toString(); }

    public static String
    trustedToken(long sessionId,long userId,Duration lifetime)
        throws JOSEException
    {
        return sign(TRUSTED,sessionId,userId,lifetime);
    }

    public static String
    forgedToken(long sessionId,long userId,Duration lifetime)
        throws JOSEException
    {
        return sign(UNTRUSTED,sessionId,userId,lifetime);
    }

    private static String
    sign(KeyPair keys,long sessionId,long userId,Duration lifetime)
        throws JOSEException
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer("granite-session")
                .audience("granite-services")
                .subject(String.valueOf(userId))
                .jwtID(String.valueOf(sessionId))
                .issueTime(Date.from(now.minus(Duration.ofMinutes(10))))
                .expirationTime(Date.from(now.plus(lifetime)))
                .build();
        SignedJWT    jwt    =
            new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(JWK.getKeyID()).build(),
                claims);

        jwt.sign(new RSASSASigner((RSAPrivateKey)keys.getPrivate()));
        return jwt.serialize();
    }

    private static KeyPair
    generate()
    {
        try
        {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);
            return generator.generateKeyPair();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static RSAKey
    publicKey()
    {
        try
        {
            return
                new RSAKey.Builder((RSAPublicKey)TRUSTED.getPublic())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyIDFromThumbprint()
                    .build();
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
