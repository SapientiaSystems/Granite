//////////////////////////////////////////////////////////////////////////////
// GraniteJwtDecoderFactoryTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.client.RestTemplate;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteJwtDecoderFactoryTest
{
    private static final String ISSUER   = "granite-session";
    private static final String AUDIENCE = "granite-services";

    private static KeyPair    trusted;
    private static KeyPair    untrusted;
    private static String     keyId;
    private static HttpServer server;
    private static JwtDecoder decoder;

    @BeforeAll
    public static void
    startJwkSetServer()
        throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);
        trusted   = generator.generateKeyPair();
        untrusted = generator.generateKeyPair();

        RSAKey jwk =
            new RSAKey.Builder((RSAPublicKey)trusted.getPublic())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyIDFromThumbprint()
                .build();
        byte[] body = new JWKSet(jwk).toString().getBytes(StandardCharsets.UTF_8);

        keyId  = jwk.getKeyID();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext(
            "/.well-known/jwks.json",
            exchange ->
            {
                exchange.getResponseHeaders().add("Content-Type","application/json");
                exchange.sendResponseHeaders(200,body.length);

                try (OutputStream out = exchange.getResponseBody())
                {
                    out.write(body);
                }
            });
        server.start();

        decoder =
            new GraniteJwtDecoderFactory()
                .create(
                    GraniteResourceServerSettings.from(
                        environment(
                            Map.of(
                                GraniteResourceServerSettings.JWK_SET_URI,
                                "http://127.0.0.1:" + server.getAddress().getPort() + "/.well-known/jwks.json"))));
    }

    @AfterAll
    public static void
    stopJwkSetServer()
    {
        server.stop(0);
    }

    @Test
    public void
    decodesTokenSignedByPublishedKey()
        throws Exception
    {
        Jwt jwt = decoder.decode(sign(trusted,ISSUER,AUDIENCE,Duration.ofMinutes(5)));

        assertEquals("1001",jwt.getSubject());
        assertEquals(List.of("ROLE_USER"),jwt.getClaimAsStringList("authorities"));
    }

    @Test
    public void
    rejectsTokenSignedByUnknownKey()
        throws Exception
    {
        String token = sign(untrusted,ISSUER,AUDIENCE,Duration.ofMinutes(5));

        assertThrows(JwtException.class,() -> decoder.decode(token));
    }

    @Test
    public void
    rejectsWrongIssuer()
        throws Exception
    {
        String token = sign(trusted,"someone-else",AUDIENCE,Duration.ofMinutes(5));

        assertThrows(JwtException.class,() -> decoder.decode(token));
    }

    @Test
    public void
    rejectsWrongAudience()
        throws Exception
    {
        String       token = sign(trusted,ISSUER,"other-services",Duration.ofMinutes(5));
        JwtException e     = assertThrows(JwtException.class,() -> decoder.decode(token));

        assertTrue(e.getMessage().contains("aud"),e.getMessage());
    }

    @Test
    public void
    rejectsExpiredToken()
        throws Exception
    {
        String token = sign(trusted,ISSUER,AUDIENCE,Duration.ofMinutes(-5));

        assertThrows(JwtException.class,() -> decoder.decode(token));
    }

    @Test
    public void
    settingsReadDefaultsAndCommaSeparatedAudience()
    {
        GraniteResourceServerSettings defaults =
            GraniteResourceServerSettings.from(
                environment(Map.of(GraniteResourceServerSettings.JWK_SET_URI,"http://x/jwks")));
        GraniteResourceServerSettings custom   =
            GraniteResourceServerSettings.from(
                environment(
                    Map.of(
                        GraniteResourceServerSettings.JWK_SET_URI,"http://x/jwks",
                        GraniteResourceServerSettings.ISSUER,"issuer-x",
                        GraniteResourceServerSettings.AUDIENCE,"a, b")));

        assertEquals("granite-session",defaults.getIssuer());
        assertEquals(List.of("granite-services"),defaults.getAudiences());
        assertEquals("issuer-x",custom.getIssuer());
        assertEquals(List.of("a","b"),custom.getAudiences());
    }

    @Test
    public void
    remoteDecoderFetchesJwkSetThroughSuppliedRestOperations()
        throws Exception
    {
        AtomicInteger calls = new AtomicInteger();
        RestTemplate  rest  = new RestTemplate();

        rest.getInterceptors().add(
            (request,body,execution) ->
            {
                calls.incrementAndGet();
                return execution.execute(request,body);
            });

        JwtDecoder custom =
            new GraniteJwtDecoderFactory()
                .create(
                    GraniteResourceServerSettings.from(
                        environment(
                            Map.of(
                                GraniteResourceServerSettings.JWK_SET_URI,
                                "http://127.0.0.1:" + server.getAddress().getPort() + "/.well-known/jwks.json"))),
                    rest);

        assertEquals("1001",custom.decode(sign(trusted,ISSUER,AUDIENCE,Duration.ofMinutes(5))).getSubject());
        assertTrue(calls.get() >= 1,"JWK set should be fetched through the supplied RestOperations");
    }

    @Test
    public void
    jwkSetUriIsRequiredForRemoteDecoder()
    {
        GraniteResourceServerSettings settings = GraniteResourceServerSettings.from(environment(Map.of()));

        assertTrue(settings.getJwkSetUri().isEmpty());
        assertThrows(IllegalStateException.class,() -> new GraniteJwtDecoderFactory().create(settings));
    }

    @Test
    public void
    inProcessDecoderUsesSuppliedJwkSetAndValidators()
        throws Exception
    {
        JwtDecoder local =
            new GraniteJwtDecoderFactory()
                .create(
                    new JWKSet(
                        new RSAKey.Builder((RSAPublicKey)trusted.getPublic())
                            .keyID(keyId)
                            .build())
                        .toPublicJWKSet()
                        .toJSONObject(),
                    GraniteResourceServerSettings.from(environment(Map.of())));
        String     forged = sign(untrusted,ISSUER,AUDIENCE,Duration.ofMinutes(5));
        String     other  = sign(trusted,ISSUER,"other-services",Duration.ofMinutes(5));

        assertEquals("1001",local.decode(sign(trusted,ISSUER,AUDIENCE,Duration.ofMinutes(5))).getSubject());
        assertThrows(JwtException.class,() -> local.decode(forged));
        assertThrows(JwtException.class,() -> local.decode(other));
    }

    private static String
    sign(KeyPair keys,String issuer,String audience,Duration lifetime)
        throws JOSEException
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject("1001")
                .jwtID("2002")
                .audience(audience)
                .issueTime(Date.from(now.minus(Duration.ofMinutes(10))))
                .expirationTime(Date.from(now.plus(lifetime)))
                .claim("authorities",List.of("ROLE_USER"))
                .build();
        SignedJWT    jwt    =
            new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build(),claims);

        jwt.sign(new RSASSASigner((RSAPrivateKey)keys.getPrivate()));
        return jwt.serialize();
    }

    private static StandardEnvironment
    environment(Map<String,Object> properties)
    {
        StandardEnvironment environment = new StandardEnvironment();

        environment.getPropertySources().addFirst(new MapPropertySource("test",properties));
        return environment;
    }
}

//////////////////////////////////////////////////////////////////////////////
