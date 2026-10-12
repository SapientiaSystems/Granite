//////////////////////////////////////////////////////////////////////////////
// Oauth2IdTokenValidatorTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class Oauth2IdTokenValidatorTest
{
    private static final String PROVIDER = "google";
    private static final String ISSUER   = "https://accounts.google.com";
    private static final String AUDIENCE = "granite-client-id";
    private static final String JWKS_PATH = "/.well-known/jwks.json";

    private WireMockServer wireMockServer;
    private RSAKey         signingKey;
    private String         jwksUri;

    @BeforeEach
    public void
    setUp()
        throws JOSEException
    {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        jwksUri        = wireMockServer.baseUrl() + JWKS_PATH;
        signingKey     =
            new RSAKeyGenerator(2048)
                .keyID(UUID.randomUUID().toString())
                .generate();

        stubJwks(signingKey);
    }

    @AfterEach
    public void
    tearDown()
    {
        wireMockServer.stop();
    }

    @Test
    public void
    acceptsValidIdToken()
        throws JOSEException
    {
        Oauth2IdTokenValidator  target   = newValidator();
        String                  token    =
            signToken(
                signingKey,
                ISSUER,
                AUDIENCE,
                "user-123",
                "user@example.com",
                Instant.now().plusSeconds(300));
        Oauth2ValidatedIdentity identity = target.validate(PROVIDER,token);

        assertEquals(PROVIDER,identity.getProvider());
        assertEquals("user-123",identity.getSub());
        assertTrue(identity.getEmail().isPresent());
        assertEquals("user@example.com",identity.getEmail().get());
    }

    @Test
    public void
    rejectsWrongAudience()
        throws JOSEException
    {
        Oauth2IdTokenValidator     target = newValidator();
        String                     token  =
            signToken(
                signingKey,
                ISSUER,
                "some-other-audience",
                "user-123",
                "user@example.com",
                Instant.now().plusSeconds(300));
        Oauth2ValidationException  ex     =
            assertThrows(
                Oauth2ValidationException.class,
                () -> target.validate(PROVIDER,token));

        assertTrue(
            ex.getMessage().toLowerCase().contains("audience"),
            "expected 'audience' in message but got: " + ex.getMessage());
    }

    @Test
    public void
    rejectsWrongIssuer()
        throws JOSEException
    {
        Oauth2IdTokenValidator    target = newValidator();
        String                    token  =
            signToken(
                signingKey,
                "https://evil.example.com",
                AUDIENCE,
                "user-123",
                "user@example.com",
                Instant.now().plusSeconds(300));
        Oauth2ValidationException ex     =
            assertThrows(
                Oauth2ValidationException.class,
                () -> target.validate(PROVIDER,token));

        assertTrue(
            ex.getMessage().toLowerCase().contains("issuer"),
            "expected 'issuer' in message but got: " + ex.getMessage());
    }

    @Test
    public void
    rejectsExpired()
        throws JOSEException
    {
        Oauth2IdTokenValidator    target = newValidator();
        String                    token  =
            signToken(
                signingKey,
                ISSUER,
                AUDIENCE,
                "user-123",
                "user@example.com",
                Instant.now().minusSeconds(60));
        Oauth2ValidationException ex     =
            assertThrows(
                Oauth2ValidationException.class,
                () -> target.validate(PROVIDER,token));

        assertTrue(
            ex.getMessage().toLowerCase().contains("expired"),
            "expected 'expired' in message but got: " + ex.getMessage());
    }

    @Test
    public void
    rejectsSignatureFromDifferentKey()
        throws JOSEException
    {
        RSAKey                    alienKey =
            new RSAKeyGenerator(2048)
                .keyID(UUID.randomUUID().toString())
                .generate();
        Oauth2IdTokenValidator    target   = newValidator();
        String                    token    =
            signToken(
                alienKey,
                ISSUER,
                AUDIENCE,
                "user-123",
                "user@example.com",
                Instant.now().plusSeconds(300));
        Oauth2ValidationException ex       =
            assertThrows(
                Oauth2ValidationException.class,
                () -> target.validate(PROVIDER,token));
        String                    message  = ex.getMessage().toLowerCase();

        assertTrue(
            message.contains("signature")
                || message.contains("rejected")
                || message.contains("invalid"),
            "expected signature error but got: " + ex.getMessage());
    }

    @Test
    public void
    rejectsUnknownProviderName()
        throws JOSEException
    {
        Oauth2IdTokenValidator    target = newValidator();
        String                    token  =
            signToken(
                signingKey,
                ISSUER,
                AUDIENCE,
                "user-123",
                "user@example.com",
                Instant.now().plusSeconds(300));
        Oauth2ValidationException ex     =
            assertThrows(
                Oauth2ValidationException.class,
                () -> target.validate("unknown-provider",token));

        assertTrue(
            ex.getMessage().toLowerCase().contains("unknown provider"),
            "expected 'unknown provider' in message but got: " + ex.getMessage());
        assertFalse(
            ex.getMessage().toLowerCase().contains("signature"));
    }

    private Oauth2IdTokenValidator
    newValidator()
    {
        Oauth2ProviderConfig      config =
            new Oauth2ProviderConfig()
                .setName(PROVIDER)
                .setIssuerUri(ISSUER)
                .setAudience(AUDIENCE)
                .setJwksUri(jwksUri);
        Oauth2ProvidersProperties props  = new Oauth2ProvidersProperties();

        props.getProviders().put(PROVIDER,config);
        return new Oauth2IdTokenValidator(props);
    }

    private void
    stubJwks(RSAKey publicKey)
    {
        JWKSet set  = new JWKSet(publicKey.toPublicJWK());
        String json = set.toJSONObject().toString();

        wireMockServer.stubFor(
            get(urlEqualTo(JWKS_PATH))
                .willReturn(okJson(json)));
    }

    private String
    signToken(
        RSAKey   key,
        String   issuer,
        String   audience,
        String   subject,
        String   email,
        Instant  expiresAt)
        throws JOSEException
    {
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(subject)
                .claim("email",email)
                .issueTime(Date.from(expiresAt.minusSeconds(300)))
                .expirationTime(Date.from(expiresAt))
                .build();
        SignedJWT    signed =
            new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(key.getKeyID())
                    .build(),
                claims);

        signed.sign(new RSASSASigner(key.toRSAPrivateKey()));
        return signed.serialize();
    }
}

//////////////////////////////////////////////////////////////////////////////
