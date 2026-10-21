//////////////////////////////////////////////////////////////////////////////
// GatewayTestBackend.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.springframework.test.context.DynamicPropertyRegistry;

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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public
class GatewayTestBackend
{
    private static final Pattern SESSION_ID = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"");

    private final KeyPair       trusted;
    private final KeyPair       untrusted;
    private final String        keyId;
    private final HttpServer    server;
    private final List<String>  receivedAuthorizations;
    private final Set<String>   revokedSessionIds;
    private final Set<String>   failingSessionIds;
    private final List<String>  sessionLookups;
    private final List<String>  lookupAuthorizations;
    private final AtomicLong    nextSessionId;

    public
    GatewayTestBackend()
        throws NoSuchAlgorithmException,IOException,JOSEException
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);
        trusted                = generator.generateKeyPair();
        untrusted              = generator.generateKeyPair();
        receivedAuthorizations = new CopyOnWriteArrayList<>();
        revokedSessionIds      = ConcurrentHashMap.newKeySet();
        failingSessionIds      = ConcurrentHashMap.newKeySet();
        sessionLookups         = new CopyOnWriteArrayList<>();
        lookupAuthorizations   = new CopyOnWriteArrayList<>();
        nextSessionId          = new AtomicLong(5000L);

        RSAKey jwk  = new RSAKey.Builder((RSAPublicKey)trusted.getPublic()).keyIDFromThumbprint().build();
        byte[] jwks = new JWKSet(jwk).toString().getBytes(StandardCharsets.UTF_8);

        keyId  = jwk.getKeyID();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/.well-known/jwks.json",exchange -> respond(exchange,jwks));
        server.createContext("/session-query-service/find-sessions-by-key",this::lookupSession);
        server.createContext(
            "/",
            exchange ->
            {
                String authorization = exchange.getRequestHeaders().getFirst("Authorization");

                receivedAuthorizations.add(authorization != null ? authorization : "");
                respond(exchange,"{\"success\":true}".getBytes(StandardCharsets.UTF_8));
            });
        server.start();
    }

    public void
    registerProperties(DynamicPropertyRegistry registry)
    {
        String base = "http://127.0.0.1:" + server.getAddress().getPort();

        registry.add("granite.security.jwt.jwk-set-uri",() -> base + "/.well-known/jwks.json");
        registry.add("granite.user-command-service.host",() -> base);
        registry.add("granite.user-query-service.host",() -> base);
        registry.add("granite.session-command-service.host",() -> base);
        registry.add("granite.session-query-service.host",() -> base);
        registry.add("granite.frontendcontroller-user-service.host",() -> base);
        registry.add("granite.frontendcontroller-admin-service.host",() -> base);
        registry.add("granite.outbox-service.host",() -> base);
    }

    public String
    sign(List<String> authorities)
        throws JOSEException
    {
        return sign(trusted,"1001",newSessionId(),authorities);
    }

    public String
    sign(String sessionId,List<String> authorities)
        throws JOSEException
    {
        return sign(trusted,"1001",sessionId,authorities);
    }

    public String
    signServiceToken(List<String> authorities)
        throws JOSEException
    {
        return sign(trusted,"service:granite-session",UUID.randomUUID().toString(),authorities);
    }

    public String
    signWithUntrustedKey(List<String> authorities)
        throws JOSEException
    {
        return sign(untrusted,"1001",newSessionId(),authorities);
    }

    public String
    newSessionId() { return String.valueOf(nextSessionId.incrementAndGet()); }

    public void
    revoke(String sessionId) { revokedSessionIds.add(sessionId); }

    public void
    failLookupsFor(String sessionId) { failingSessionIds.add(sessionId); }

    public List<String>
    getSessionLookups() { return sessionLookups; }

    public List<String>
    getLookupAuthorizations() { return lookupAuthorizations; }

    public List<String>
    getReceivedAuthorizations() { return receivedAuthorizations; }

    public void
    stop()
    {
        server.stop(0);
    }

    private void
    lookupSession(HttpExchange exchange)
        throws IOException
    {
        String  body      = new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
        Matcher matcher   = SESSION_ID.matcher(body);
        String  sessionId = matcher.find() ? matcher.group(1) : "";

        sessionLookups.add(sessionId);
        lookupAuthorizations.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));

        if (failingSessionIds.contains(sessionId))
        {
            exchange.sendResponseHeaders(500,-1);
            exchange.close();
            return;
        }

        String reply =
            revokedSessionIds.contains(sessionId)
                ? "{\"success\":true,\"sessions\":[]}"
                : "{\"success\":true,\"sessions\":[{\"sessionId\":\"" + sessionId
                    + "\",\"expires\":\"" + Instant.now().plusSeconds(3600) + "\"}]}";

        respond(exchange,reply.getBytes(StandardCharsets.UTF_8));
    }

    private String
    sign(KeyPair keys,String subject,String sessionId,List<String> authorities)
        throws JOSEException
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer("granite-session")
                .subject(subject)
                .jwtID(sessionId)
                .audience("granite-services")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .claim("authorities",authorities)
                .build();
        SignedJWT    jwt    =
            new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build(),claims);

        jwt.sign(new RSASSASigner((RSAPrivateKey)keys.getPrivate()));
        return jwt.serialize();
    }

    private static void
    respond(HttpExchange exchange,byte[] body)
        throws IOException
    {
        exchange.getResponseHeaders().add("Content-Type","application/json");
        exchange.sendResponseHeaders(200,body.length);

        try (OutputStream out = exchange.getResponseBody())
        {
            out.write(body);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
