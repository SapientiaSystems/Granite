//////////////////////////////////////////////////////////////////////////////
// JwkSetProviderTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import granite.session.server.domain.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class JwkSetProviderTest
{
    private static KeyPair current;
    private static KeyPair previous;

    @BeforeAll
    public static void
    generateKeys()
        throws NoSuchAlgorithmException
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);
        current  = generator.generateKeyPair();
        previous = generator.generateKeyPair();
    }

    @Test
    public void
    publishesOnlyPublicSigningKeyWithThumbprintKid()
        throws ParseException
    {
        JwkSetProvider subject = new JwkSetProvider(List.of(publicKey(current)));
        JWKSet         parsed  = JWKSet.parse(subject.getPublicJwkSet());
        RSAKey         key     = parsed.getKeys().get(0).toRSAKey();

        assertEquals(1,parsed.getKeys().size());
        assertEquals(newTokenService(current).getKeyId(),key.getKeyID());
        assertEquals(List.of(key.getKeyID()),subject.getKeyIds());
        assertEquals("sig",key.getKeyUse().identifier());
        assertEquals("RS256",key.getAlgorithm().getName());
        assertFalse(key.isPrivate());
        assertFalse(subject.getPublicJwkSet().toString().contains("\"d\""));
    }

    @Test
    public void
    mintedJwtVerifiesAgainstPublishedKey()
        throws ParseException,JOSEException
    {
        JwkSetProvider subject = new JwkSetProvider(List.of(publicKey(current)));
        String         token   = newTokenService(current).mint(newSession());
        SignedJWT      jwt     = SignedJWT.parse(token);
        RSAKey         key     =
            JWKSet
                .parse(subject.getPublicJwkSet())
                .getKeyByKeyId(jwt.getHeader().getKeyID())
                .toRSAKey();

        assertNotNull(key);
        assertTrue(jwt.verify(new RSASSAVerifier(key)));
    }

    @Test
    public void
    publishesCurrentAndPreviousKeysForRotation()
        throws ParseException,JOSEException
    {
        JwkSetProvider subject =
            new JwkSetProvider(List.of(publicKey(current),publicKey(previous)));
        String         oldJwt  = newTokenService(previous).mint(newSession());
        SignedJWT      jwt     = SignedJWT.parse(oldJwt);
        JWKSet         parsed  = JWKSet.parse(subject.getPublicJwkSet());

        assertEquals(2,parsed.getKeys().size());
        assertNotEquals(subject.getKeyIds().get(0),subject.getKeyIds().get(1));
        assertTrue(
            jwt.verify(
                new RSASSAVerifier(
                    parsed.getKeyByKeyId(jwt.getHeader().getKeyID()).toRSAKey())));
    }

    @Test
    public void
    duplicateKeysArePublishedOnce()
    {
        JwkSetProvider subject =
            new JwkSetProvider(List.of(publicKey(current),publicKey(current)));

        assertEquals(1,subject.getKeyIds().size());
    }

    @Test
    public void
    rejectsEmptyKeyList()
    {
        assertThrows(IllegalArgumentException.class,() -> new JwkSetProvider(List.of()));
    }

    private static RSAPublicKey
    publicKey(KeyPair pair) { return (RSAPublicKey)pair.getPublic(); }

    private static JwtTokenService
    newTokenService(KeyPair pair)
    {
        return
            new JwtTokenService(
                (RSAPrivateKey)pair.getPrivate(),
                (RSAPublicKey)pair.getPublic(),
                "granite-session",
                List.of("granite-services"),
                Duration.ofMinutes(15));
    }

    private static Session
    newSession()
    {
        return
            new Session()
                .setSessionId(2002L)
                .setUserId(1001L)
                .setDeviceId("device-A")
                .setExpires(Instant.now().plus(Duration.ofMinutes(15)));
    }
}

//////////////////////////////////////////////////////////////////////////////
