//////////////////////////////////////////////////////////////////////////////
// SessionQueryHostSmokeTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.queryhost.main;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
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
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    classes = SessionQueryHost.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "DEPLOY_ENV=local")
@ActiveProfiles({"local"})
public
class SessionQueryHostSmokeTest
{
    private static final String QUERY_PATH = "/session-query-service/find-sessions-by-key";
    private static final String QUERY_BODY = "{\"key\":{\"name\":\"sessionId\",\"value\":\"999999\"}}";

    private static KeyPair    trusted;
    private static KeyPair    untrusted;
    private static String     keyId;
    private static HttpServer jwkSetServer;

    @Autowired
    private TestRestTemplate rest;

    @DynamicPropertySource
    static void
    jwkSetUri(DynamicPropertyRegistry registry)
        throws Exception
    {
        startJwkSetServer();
        registry.add(
            "granite.security.jwt.jwk-set-uri",
            () -> "http://127.0.0.1:" + jwkSetServer.getAddress().getPort() + "/.well-known/jwks.json");
    }

    @AfterAll
    public static void
    stopJwkSetServer()
    {
        jwkSetServer.stop(0);
    }

    @Test
    public void
    queryEndpointRejectsMissingToken()
    {
        ResponseEntity<String> response = post(null);

        assertEquals(401,response.getStatusCode().value());
        assertTrue(
            response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer"),
            "expected a Bearer challenge");
    }

    @Test
    public void
    queryEndpointRejectsTokenFromUntrustedKey()
        throws JOSEException
    {
        assertEquals(401,post(sign(untrusted)).getStatusCode().value());
    }

    @Test
    public void
    queryEndpointAcceptsValidToken()
        throws JOSEException
    {
        ResponseEntity<String> response = post(sign(trusted));

        assertEquals(200,response.getStatusCode().value(),response.getBody());
    }

    private ResponseEntity<String>
    post(String token)
    {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        if (token != null)
            headers.setBearerAuth(token);

        return rest.postForEntity(QUERY_PATH,new HttpEntity<>(QUERY_BODY,headers),String.class);
    }

    private static String
    sign(KeyPair keys)
        throws JOSEException
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer("granite-session")
                .subject("1001")
                .jwtID("2002")
                .audience("granite-services")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .claim("authorities",List.of("ROLE_User"))
                .build();
        SignedJWT    jwt    =
            new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build(),claims);

        jwt.sign(new RSASSASigner((RSAPrivateKey)keys.getPrivate()));
        return jwt.serialize();
    }

    private static void
    startJwkSetServer()
        throws NoSuchAlgorithmException,IOException,JOSEException
    {
        if (jwkSetServer != null)
            return;

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);
        trusted   = generator.generateKeyPair();
        untrusted = generator.generateKeyPair();

        RSAKey jwk  =
            new RSAKey.Builder((RSAPublicKey)trusted.getPublic())
                .keyIDFromThumbprint()
                .build();
        byte[] body = new JWKSet(jwk).toString().getBytes(StandardCharsets.UTF_8);

        keyId        = jwk.getKeyID();
        jwkSetServer = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        jwkSetServer.createContext(
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
        jwkSetServer.start();
    }
}

//////////////////////////////////////////////////////////////////////////////
