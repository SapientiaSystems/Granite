//////////////////////////////////////////////////////////////////////////////
// JwtTokenServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import granite.session.server.domain.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class JwtTokenServiceTest
{
    private static final String ISSUER   = "granite-session";
    private static final String AUDIENCE = "granite-services";
    private static final Duration TTL    = Duration.ofMinutes(15);

    private static RSAPrivateKey privateKey;
    private static RSAPublicKey  publicKey;

    @BeforeAll
    public static void
    loadKeys()
        throws IOException,NoSuchAlgorithmException,InvalidKeySpecException
    {
        privateKey = loadPrivateKey("/jwt-test-private-key.pem");
        publicKey  = loadPublicKey("/jwt-test-public-key.pem");
    }

    @Test
    public void
    mintedJwtContainsRequiredClaims()
        throws ParseException
    {
        JwtTokenService target  = newService();
        Session         session = newSession(1001L,2002L,"device-A");
        String          token   = target.mint(session);
        JWTClaimsSet    claims  = SignedJWT.parse(token).getJWTClaimsSet();

        assertEquals(ISSUER,claims.getIssuer());
        assertEquals("1001",claims.getSubject());
        assertEquals("2002",claims.getJWTID());
        assertNotNull(claims.getIssueTime());
        assertNotNull(claims.getExpirationTime());
        assertTrue(claims.getAudience().contains(AUDIENCE));
        assertEquals("device-A",claims.getStringClaim("did"));
    }

    @Test
    public void
    verifyAcceptsValidJwt()
        throws JwtVerificationException
    {
        JwtTokenService         target  = newService();
        Session                 session = newSession(1001L,2002L,"device-A");
        String                  token   = target.mint(session);
        JwtVerificationResult   result  = target.verifySignatureAndExp(token);

        assertEquals("1001",result.getSubject());
        assertEquals("2002",result.getJwtId());
        assertEquals("device-A",result.getDeviceId());
        assertTrue(result.getAudience().contains(AUDIENCE));
    }

    @Test
    public void
    verifyRejectsExpiredJwt()
    {
        JwtTokenService target  = newService();
        Session         session =
            new Session()
                .setSessionId(2002L)
                .setUserId(1001L)
                .setDeviceId("device-A")
                .setExpires(Instant.now().minusSeconds(60));
        String                   token = target.mint(session);
        JwtVerificationException ex    =
            assertThrows(
                JwtVerificationException.class,
                () -> target.verifySignatureAndExp(token));

        assertTrue(
            ex.getMessage().toLowerCase().contains("expired"),
            "expected 'expired' in message but got: " + ex.getMessage());
    }

    @Test
    public void
    verifyRejectsWrongAudience()
    {
        JwtTokenService          minter   = newService("granite-session",AUDIENCE);
        JwtTokenService          verifier = newService("granite-session","other-audience");
        Session                  session  = newSession(1001L,2002L,"device-A");
        String                   token    = minter.mint(session);
        JwtVerificationException ex       =
            assertThrows(
                JwtVerificationException.class,
                () -> verifier.verifySignatureAndExp(token));

        assertTrue(
            ex.getMessage().toLowerCase().contains("audience"),
            "expected 'audience' in message but got: " + ex.getMessage());
    }

    @Test
    public void
    verifyRejectsWrongIssuer()
    {
        JwtTokenService          minter   = newService("other-issuer",AUDIENCE);
        JwtTokenService          verifier = newService(ISSUER,AUDIENCE);
        Session                  session  = newSession(1001L,2002L,"device-A");
        String                   token    = minter.mint(session);
        JwtVerificationException ex       =
            assertThrows(
                JwtVerificationException.class,
                () -> verifier.verifySignatureAndExp(token));

        assertTrue(
            ex.getMessage().toLowerCase().contains("issuer"),
            "expected 'issuer' in message but got: " + ex.getMessage());
    }

    @Test
    public void
    verifyRejectsTamperedSignature()
        throws JOSEException
    {
        JwtTokenService target   = newService();
        Session         session  = newSession(1001L,2002L,"device-A");
        // Mint claims legitimately then re-sign with a different (random) key.
        JWTClaimsSet    claims   =
            new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject("1001")
                .jwtID("2002")
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plus(TTL)))
                .audience(AUDIENCE)
                .claim("did","device-A")
                .build();
        SignedJWT       alien    = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256),claims);

        alien.sign(new RSASSASigner(generateAlienPrivateKey()));

        String                   token = alien.serialize();
        JwtVerificationException ex    =
            assertThrows(
                JwtVerificationException.class,
                () -> target.verifySignatureAndExp(token));

        assertTrue(
            ex.getMessage().toLowerCase().contains("signature")
                || ex.getMessage().toLowerCase().contains("parse"),
            "expected signature/parse error but got: " + ex.getMessage());
    }

    @Test
    public void
    mintedJwtJtiEqualsSessionId()
        throws ParseException
    {
        JwtTokenService target  = newService();
        Session         session = newSession(777L,12345L,"device-X");
        String          token   = target.mint(session);
        JWTClaimsSet    claims  = SignedJWT.parse(token).getJWTClaimsSet();

        assertEquals("12345",claims.getJWTID());
        assertEquals(String.valueOf(session.getSessionId()),claims.getJWTID());
    }

    @Test
    public void
    mintedJwtContainsAuthoritiesClaim()
        throws ParseException
    {
        JwtTokenService target      = newService();
        Set<String>     authorities = new LinkedHashSet<>(List.of("ROLE_USER","ROLE_ADMIN"));
        Session         session     =
            newSession(1001L,2002L,"device-A").setAuthorities(authorities);
        String          token       = target.mint(session);
        JWTClaimsSet    claims      = SignedJWT.parse(token).getJWTClaimsSet();
        List<String>    actual      = claims.getStringListClaim("authorities");

        assertNotNull(actual);
        assertEquals(List.of("ROLE_USER","ROLE_ADMIN"),actual);
    }

    @Test
    public void
    verifyExposesAuthoritiesClaim()
        throws JwtVerificationException
    {
        JwtTokenService       target      = newService();
        Set<String>           authorities = new LinkedHashSet<>(List.of("ROLE_USER","ROLE_ADMIN"));
        Session               session     =
            newSession(1001L,2002L,"device-A").setAuthorities(authorities);
        String                token       = target.mint(session);
        JwtVerificationResult result      = target.verifySignatureAndExp(token);

        assertEquals(authorities,result.getAuthorities());
    }

    @Test
    public void
    mintedJwtWithEmptyAuthoritiesEmitsEmptyClaim()
        throws ParseException
    {
        JwtTokenService target  = newService();
        Session         session = newSession(1001L,2002L,"device-A");
        String          token   = target.mint(session);
        JWTClaimsSet    claims  = SignedJWT.parse(token).getJWTClaimsSet();
        List<String>    actual  = claims.getStringListClaim("authorities");

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void
    mintedJwtExpirationIsCappedAtTtl()
        throws ParseException
    {
        JwtTokenService target  = newService();
        Instant         before  = Instant.now();
        Session         session =
            newSession(1001L,2002L,"device-A").setExpires(Instant.now().plus(Duration.ofHours(8)));
        JWTClaimsSet    claims  = SignedJWT.parse(target.mint(session)).getJWTClaimsSet();
        Instant         exp     = claims.getExpirationTime().toInstant();

        assertFalse(exp.isAfter(Instant.now().plus(TTL)),"exp should not exceed now + ttl");
        assertFalse(exp.isBefore(before.plus(TTL).minusSeconds(1)),"exp should be about now + ttl");
    }

    @Test
    public void
    mintedJwtExpirationNeverOutlivesSession()
        throws ParseException
    {
        JwtTokenService target   = newService();
        Instant         expires  = Instant.now().plus(Duration.ofMinutes(3)).truncatedTo(ChronoUnit.SECONDS);
        Session         session  = newSession(1001L,2002L,"device-A").setExpires(expires);
        JWTClaimsSet    claims   = SignedJWT.parse(target.mint(session)).getJWTClaimsSet();

        assertEquals(expires,claims.getExpirationTime().toInstant());
    }

    @Test
    public void
    mintedJwtHeaderCarriesThumbprintKeyId()
        throws ParseException,JOSEException
    {
        JwtTokenService target   = newService();
        SignedJWT       parsed   = SignedJWT.parse(target.mint(newSession(1001L,2002L,"device-A")));
        String          expected = new RSAKey.Builder(publicKey).keyIDFromThumbprint().build().getKeyID();

        assertEquals(expected,parsed.getHeader().getKeyID());
        assertEquals(expected,target.getKeyId());
        assertEquals(JOSEObjectType.JWT,parsed.getHeader().getType());
    }

    @Test
    public void
    mintedServiceTokenCarriesServiceIdentity()
        throws ParseException,JwtVerificationException
    {
        JwtTokenService target = newService();
        String          token  =
            target.mintServiceToken("service:granite-session",List.of("SCOPE_internal"),Duration.ofMinutes(5));
        SignedJWT       parsed = SignedJWT.parse(token);
        JWTClaimsSet    claims = parsed.getJWTClaimsSet();

        assertEquals("service:granite-session",claims.getSubject());
        assertEquals(List.of("SCOPE_internal"),claims.getStringListClaim("authorities"));
        assertTrue(claims.getAudience().contains(AUDIENCE));
        assertNotNull(claims.getJWTID());
        assertEquals(target.getKeyId(),parsed.getHeader().getKeyID());
        assertFalse(claims.getExpirationTime().toInstant().isAfter(Instant.now().plus(Duration.ofMinutes(5))));
        assertEquals("service:granite-session",target.verifySignatureAndExp(token).getSubject());
    }

    private JwtTokenService
    newService()
    {
        return newService(ISSUER,AUDIENCE);
    }

    private JwtTokenService
    newService(String issuer,String audience)
    {
        return new JwtTokenService(privateKey,publicKey,issuer,List.of(audience),TTL);
    }

    private Session
    newSession(Long userId,Long sessionId,String deviceId)
    {
        return
            new Session()
                .setSessionId(sessionId)
                .setUserId(userId)
                .setDeviceId(deviceId)
                .setExpires(Instant.now().plus(TTL));
    }

    private static RSAPrivateKey
    loadPrivateKey(String resource)
        throws IOException,NoSuchAlgorithmException,InvalidKeySpecException
    {
        byte[] bytes = decodePem(readResource(resource),"PRIVATE KEY");

        return
            (RSAPrivateKey)
                KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    private static RSAPublicKey
    loadPublicKey(String resource)
        throws IOException,NoSuchAlgorithmException,InvalidKeySpecException
    {
        byte[] bytes = decodePem(readResource(resource),"PUBLIC KEY");

        return
            (RSAPublicKey)
                KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(bytes));
    }

    private static String
    readResource(String resource)
        throws IOException
    {
        try (InputStream in = JwtTokenServiceTest.class.getResourceAsStream(resource))
        {
            if (in == null)
                throw new IOException("resource not found: " + resource);

            return new String(in.readAllBytes(),StandardCharsets.UTF_8);
        }
    }

    private static byte[]
    decodePem(String pem,String kind)
    {
        String begin   = "-----BEGIN " + kind + "-----";
        String end     = "-----END " + kind + "-----";
        int    s       = pem.indexOf(begin);
        int    e       = pem.indexOf(end);

        if (s < 0 || e < 0)
            throw new IllegalArgumentException("not a " + kind + " PEM");

        String payload = pem.substring(s + begin.length(),e).replaceAll("\\s+","");

        return Base64.getDecoder().decode(payload);
    }

    private static RSAPrivateKey
    generateAlienPrivateKey()
    {
        try
        {
            java.security.KeyPairGenerator generator = java.security.KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);
            return (RSAPrivateKey)generator.generateKeyPair().getPrivate();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
