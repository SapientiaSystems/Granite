//////////////////////////////////////////////////////////////////////////////
// SessionQueryRevocationChecker.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public
class SessionQueryRevocationChecker
    implements ISessionRevocationChecker
{
    public static final String SERVICE_SUBJECT_PREFIX = "service:";
    public static final String INTERNAL_SCOPE         = "SCOPE_internal";
    public static final String AUTHORITIES_CLAIM      = "authorities";
    public static final String DEVICE_ID_CLAIM        = "did";

    private static final int MAX_CACHE_ENTRIES = 10_000;

    private final WebClient            webClient;
    private final URI                  lookupUri;
    private final Duration             liveCacheTtl;
    private final Duration             timeout;
    private final Clock                clock;
    private final Map<String,Instant>  liveUntil;
    private final Map<String,Instant>  revokedUntil;
    private final Logger               logger;

    public
    SessionQueryRevocationChecker(
        WebClient webClient,
        URI       lookupUri,
        Duration  liveCacheTtl,
        Duration  timeout,
        Clock     clock)
    {
        this.webClient    = Objects.requireNonNull(webClient,"webClient");
        this.lookupUri    = Objects.requireNonNull(lookupUri,"lookupUri");
        this.liveCacheTtl = Objects.requireNonNull(liveCacheTtl,"liveCacheTtl");
        this.timeout      = Objects.requireNonNull(timeout,"timeout");
        this.clock        = Objects.requireNonNull(clock,"clock");
        this.liveUntil    = new ConcurrentHashMap<>();
        this.revokedUntil = new ConcurrentHashMap<>();
        this.logger       = LogManager.getLogger(SessionQueryRevocationChecker.class);
    }

    @Override
    public Mono<Boolean>
    isActive(Jwt jwt)
    {
        if (isServiceToken(jwt))
            return Mono.just(true);

        String  sessionId = jwt.getId();
        Instant now       = clock.instant();

        if (sessionId == null || sessionId.isBlank())
            return Mono.just(false);

        if (isCached(revokedUntil,sessionId,now))
            return Mono.just(false);

        if (isCached(liveUntil,sessionId,now))
            return Mono.just(true);

        return
            webClient
                .post()
                .uri(lookupUri)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .bodyValue(Map.of("key",Map.of("name","sessionId","value",sessionId)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(timeout)
                .map(reply -> isLive(reply,now))
                .doOnNext(active -> remember(sessionId,active,jwt,now))
                .onErrorResume(
                    e ->
                    {
                        logger.warn("session revocation lookup failed; rejecting token: {}",e.getMessage());
                        return Mono.just(false);
                    });
    }

    private static boolean
    isServiceToken(Jwt jwt)
    {
        if (jwt.getSubject() != null && jwt.getSubject().startsWith(SERVICE_SUBJECT_PREFIX))
            return true;

        List<String> authorities = jwt.getClaimAsStringList(AUTHORITIES_CLAIM);

        return
            !jwt.hasClaim(DEVICE_ID_CLAIM)
                && authorities != null
                && authorities.contains(INTERNAL_SCOPE);
    }

    private boolean
    isLive(JsonNode reply,Instant now)
    {
        JsonNode sessions = reply.path("sessions");

        if (!reply.path("success").asBoolean(false) || !sessions.isArray() || sessions.isEmpty())
            return false;

        Instant expires = readInstant(sessions.get(0).path("expires"));

        return expires != null && expires.isAfter(now);
    }

    private void
    remember(String sessionId,boolean active,Jwt jwt,Instant now)
    {
        if (liveUntil.size() + revokedUntil.size() > MAX_CACHE_ENTRIES)
            evictExpired(now);

        if (active)
            liveUntil.put(sessionId,now.plus(liveCacheTtl));
        else
            revokedUntil.put(sessionId,jwt.getExpiresAt() != null ? jwt.getExpiresAt() : now.plus(liveCacheTtl));
    }

    private void
    evictExpired(Instant now)
    {
        liveUntil.values().removeIf(until -> !until.isAfter(now));
        revokedUntil.values().removeIf(until -> !until.isAfter(now));
    }

    private static boolean
    isCached(Map<String,Instant> cache,String sessionId,Instant now)
    {
        Instant until = cache.get(sessionId);

        return until != null && until.isAfter(now);
    }

    private static Instant
    readInstant(JsonNode node)
    {
        try
        {
            if (node.isNumber())
            {
                BigDecimal seconds = node.decimalValue();

                return
                    Instant.ofEpochSecond(
                        seconds.longValue(),
                        seconds.remainder(BigDecimal.ONE).movePointRight(9).longValue());
            }

            if (node.isTextual())
                return Instant.parse(node.asText());
        }
        catch (DateTimeParseException | ArithmeticException e)
        {
            return null;
        }

        return null;
    }
}

//////////////////////////////////////////////////////////////////////////////
