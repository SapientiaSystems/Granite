//////////////////////////////////////////////////////////////////////////////
// SessionCommandSessionResolver.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jwt.JWTParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public
class SessionCommandSessionResolver
    implements ISessionResolver
{
    private static final int MAX_CACHE_ENTRIES = 10_000;

    private final WebClient                    webClient;
    private final URI                          resolveUri;
    private final Duration                     cacheTtl;
    private final Duration                     timeout;
    private final Clock                        clock;
    private final Map<String,ResolvedSession>  cache;
    private final Logger                       logger;

    public
    SessionCommandSessionResolver(
        WebClient webClient,
        URI       resolveUri,
        Duration  cacheTtl,
        Duration  timeout,
        Clock     clock)
    {
        this.webClient     = Objects.requireNonNull(webClient,"webClient");
        this.resolveUri    = Objects.requireNonNull(resolveUri,"resolveUri");
        this.cacheTtl      = Objects.requireNonNull(cacheTtl,"cacheTtl");
        this.timeout       = Objects.requireNonNull(timeout,"timeout");
        this.clock         = Objects.requireNonNull(clock,"clock");
        this.cache         = new ConcurrentHashMap<>();
        this.logger        = LogManager.getLogger(SessionCommandSessionResolver.class);
    }

    @Override
    public Mono<String>
    resolve(String handle)
    {
        if (handle == null || handle.isBlank())
            return Mono.empty();

        String          key    = hash(handle);
        Instant         now    = clock.instant();
        ResolvedSession cached = cache.get(key);

        if (cached != null && cached.isLiveAt(now))
            return Mono.just(cached.getAccessToken());

        return
            callSession(handle,now)
                .timeout(timeout)
                .flatMap(this::readAccessToken)
                .doOnNext(accessToken -> remember(key,accessToken,now))
                .onErrorResume(
                    e ->
                    {
                        logger.warn("session resolution failed: {}",e.getClass().getSimpleName());
                        return Mono.empty();
                    });
    }

    Set<String>
    getCacheKeys() { return Set.copyOf(cache.keySet()); }

    private Mono<JsonNode>
    callSession(String handle,Instant now)
    {
        return
            webClient
                .post()
                .uri(resolveUri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    Map.of(
                        "requestId",UUID.randomUUID().toString(),
                        "timestamp",now.toString(),
                        "handle",handle))
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    private Mono<String>
    readAccessToken(JsonNode reply)
    {
        String accessToken = reply.path("accessToken").asText("");

        if (!reply.path("success").asBoolean(false) || accessToken.isBlank())
            return Mono.empty();

        return Mono.just(accessToken);
    }

    private void
    remember(String key,String accessToken,Instant now)
    {
        Instant expires = readExpiration(accessToken);

        if (expires == null)
            return;

        Instant until = now.plus(cacheTtl);

        if (expires.isBefore(until))
            until = expires;

        if (!until.isAfter(now))
            return;

        if (cache.size() >= MAX_CACHE_ENTRIES)
            cache.values().removeIf(entry -> !entry.isLiveAt(now));

        cache.put(key,new ResolvedSession(accessToken,until));
    }

    private Instant
    readExpiration(String accessToken)
    {
        try
        {
            Date expiration = JWTParser.parse(accessToken).getJWTClaimsSet().getExpirationTime();

            return expiration != null ? expiration.toInstant() : null;
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    private String
    hash(String handle)
    {
        try
        {
            return
                HexFormat
                    .of()
                    .formatHex(
                        MessageDigest
                            .getInstance("SHA-256")
                            .digest(handle.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("SHA-256 is unavailable",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
