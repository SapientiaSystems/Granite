//////////////////////////////////////////////////////////////////////////////
// FrontEndControllerUserHostSmokeTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.userhost.main;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
    classes = {FrontEndControllerUserHost.class,CallerProbeController.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "DEPLOY_ENV=local")
@ActiveProfiles({"local"})
public
class FrontEndControllerUserHostSmokeTest
{
    private static final KeyPair    KEYS = generate();
    private static final RSAKey     JWK  = publicKey();
    private static final HttpServer JWKS = startJwks();

    @Autowired
    private TestRestTemplate rest;

    @DynamicPropertySource
    public static void
    jwkSetUri(DynamicPropertyRegistry registry)
    {
        registry.add(
            "granite.security.jwt.jwk-set-uri",
            () -> "http://localhost:" + JWKS.getAddress().getPort() + "/.well-known/jwks.json");
        registry.add("granite.security.jwt.ssl-bundle",() -> "");
    }

    @AfterAll
    public static void
    stopJwks()
    {
        JWKS.stop(0);
    }

    @Test
    public void
    verifiedBearerYieldsTheCallerOnTheRequestThread()
        throws Exception
    {
        ResponseEntity<String> response = probe(token(11L,7L));

        assertEquals(200,response.getStatusCode().value(),response.getBody());
        assertEquals("7",response.getBody());
    }

    @Test
    public void
    anonymousRequestHasNoCaller()
    {
        ResponseEntity<String> response = probe(null);

        assertEquals(200,response.getStatusCode().value(),response.getBody());
        assertEquals("none",response.getBody());
    }

    private ResponseEntity<String>
    probe(String token)
    {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        if (token != null)
            headers.setBearerAuth(token);

        return rest.postForEntity(CallerProbeController.PATH,new HttpEntity<>("{}",headers),String.class);
    }

    private static String
    token(long sessionId,long userId)
        throws JOSEException
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer("granite-session")
                .audience("granite-services")
                .subject(String.valueOf(userId))
                .jwtID(String.valueOf(sessionId))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(Duration.ofMinutes(5))))
                .build();
        SignedJWT    jwt    =
            new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(JWK.getKeyID()).build(),
                claims);

        jwt.sign(new RSASSASigner((RSAPrivateKey)KEYS.getPrivate()));
        return jwt.serialize();
    }

    private static HttpServer
    startJwks()
    {
        try
        {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost",0),0);
            byte[]     body   = new JWKSet(JWK).toPublicJWKSet().toString().getBytes(StandardCharsets.UTF_8);

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
            return server;
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
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
                new RSAKey.Builder((RSAPublicKey)KEYS.getPublic())
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
