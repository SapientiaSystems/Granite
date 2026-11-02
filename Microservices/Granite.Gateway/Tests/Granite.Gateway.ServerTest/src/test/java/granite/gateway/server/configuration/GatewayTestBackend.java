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
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public
class GatewayTestBackend
{
    private static final Pattern SESSION_ID = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern HANDLE     = Pattern.compile("\"handle\"\\s*:\\s*\"([^\"]+)\"");

    private final KeyPair             trusted;
    private final KeyPair             untrusted;
    private final String              keyId;
    private final HttpServer          server;
    private final List<String>        receivedAuthorizations;
    private final Set<String>         revokedSessionIds;
    private final Set<String>         failingSessionIds;
    private final List<String>        sessionLookups;
    private final List<String>        lookupAuthorizations;
    private final AtomicLong          nextSessionId;
    private final List<String>        receivedCookies;
    private final List<String>        receivedBodies;
    private final Map<String,String>  handleTokens;
    private final Set<String>         failingHandles;
    private final Set<String>         delayedHandles;
    private final List<String>        resolveCalls;
    private final List<String>        resolveAuthorizations;

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
        receivedCookies        = new CopyOnWriteArrayList<>();
        receivedBodies         = new CopyOnWriteArrayList<>();
        handleTokens           = new ConcurrentHashMap<>();
        failingHandles         = ConcurrentHashMap.newKeySet();
        delayedHandles         = ConcurrentHashMap.newKeySet();
        resolveCalls           = new CopyOnWriteArrayList<>();
        resolveAuthorizations  = new CopyOnWriteArrayList<>();

        RSAKey jwk  = new RSAKey.Builder((RSAPublicKey)trusted.getPublic()).keyIDFromThumbprint().build();
        byte[] jwks = new JWKSet(jwk).toString().getBytes(StandardCharsets.UTF_8);

        keyId  = jwk.getKeyID();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/.well-known/jwks.json",exchange -> respond(exchange,jwks));
        server.createContext("/session-query-service/find-sessions-by-key",this::lookupSession);
        server.createContext("/session-command-service/resolve-session",this::resolveSession);
        server.createContext(
            "/",
            exchange ->
            {
                String authorization = exchange.getRequestHeaders().getFirst("Authorization");

                receivedAuthorizations.add(authorization != null ? authorization : "");
                receivedCookies.add(String.valueOf(exchange.getRequestHeaders().getFirst("Cookie")));
                receivedBodies.add(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
                respond(exchange,"{\"success\":true}".getBytes(StandardCharsets.UTF_8));
            });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public String
    getBaseUrl() { return "http://127.0.0.1:" + server.getAddress().getPort(); }

    public void
    registerProperties(DynamicPropertyRegistry registry)
    {
        String base = getBaseUrl();

        registry.add("granite.security.jwt.jwk-set-uri",() -> base + "/.well-known/jwks.json");
        registry.add("granite.user-command-service.host",() -> base);
        registry.add("granite.user-query-service.host",() -> base);
        registry.add("granite.session-command-service.host",() -> base);
        registry.add("granite.session-query-service.host",() -> base);
        registry.add("granite.feedback-command-service.host",() -> base);
        registry.add("granite.feedback-query-service.host",() -> base);
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
    sign(String sessionId,List<String> authorities,Duration lifetime)
        throws JOSEException
    {
        return sign(trusted,"1001",sessionId,authorities,lifetime);
    }

    public void
    registerHandle(String handle,String token) { handleTokens.put(handle,token); }

    public void
    failResolvesFor(String handle) { failingHandles.add(handle); }

    public void
    delayResolvesFor(String handle) { delayedHandles.add(handle); }

    public List<String>
    getResolveCalls() { return resolveCalls; }

    public List<String>
    getResolveAuthorizations() { return resolveAuthorizations; }

    public List<String>
    getReceivedCookies() { return receivedCookies; }

    public List<String>
    getReceivedBodies() { return receivedBodies; }

    public String
    signServiceToken(List<String> authorities)
        throws JOSEException
    {
        return
            sign(
                trusted,
                "service:granite-session",
                UUID.randomUUID().toString(),
                authorities,
                Duration.ofSeconds(300),
                false);
    }

    public String
    signClientServiceToken(String clientId)
        throws JOSEException
    {
        return
            sign(
                trusted,
                clientId,
                UUID.randomUUID().toString(),
                List.of("SCOPE_internal"),
                Duration.ofSeconds(300),
                false);
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

    private void
    resolveSession(HttpExchange exchange)
        throws IOException
    {
        String  body    = new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
        Matcher matcher = HANDLE.matcher(body);
        String  handle  = matcher.find() ? matcher.group(1) : "";

        resolveCalls.add(handle);
        resolveAuthorizations.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));

        if (delayedHandles.contains(handle))
        {
            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }

        if (failingHandles.contains(handle))
        {
            exchange.sendResponseHeaders(500,-1);
            exchange.close();
            return;
        }

        String token = handleTokens.get(handle);
        String reply =
            token == null
                ? "{\"success\":false,\"failureMessage\":\"invalid session\"}"
                : "{\"success\":true,\"session\":{\"sessionId\":1},\"accessToken\":\"" + token + "\"}";

        respond(exchange,reply.getBytes(StandardCharsets.UTF_8));
    }

    private String
    sign(KeyPair keys,String subject,String sessionId,List<String> authorities)
        throws JOSEException
    {
        return sign(keys,subject,sessionId,authorities,Duration.ofSeconds(300));
    }

    private String
    sign(KeyPair keys,String subject,String sessionId,List<String> authorities,Duration lifetime)
        throws JOSEException
    {
        return sign(keys,subject,sessionId,authorities,lifetime,true);
    }

    private String
    sign(
        KeyPair      keys,
        String       subject,
        String       sessionId,
        List<String> authorities,
        Duration     lifetime,
        boolean      session)
        throws JOSEException
    {
        Instant              now     = Instant.now();
        JWTClaimsSet.Builder builder =
            new JWTClaimsSet.Builder()
                .issuer("granite-session")
                .subject(subject)
                .jwtID(sessionId)
                .audience("granite-services")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(lifetime)))
                .claim("authorities",authorities);

        if (session)
            builder.claim("did","device-" + sessionId);

        JWTClaimsSet claims = builder.build();
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
