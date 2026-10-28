//////////////////////////////////////////////////////////////////////////////
// SessionQueryRevocationCheckerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionQueryRevocationCheckerTest
{
    private static final URI      LOOKUP  = URI.create("http://session-query.invalid/session-query-service/find-sessions-by-key");
    private static final Duration TIMEOUT = Duration.ofSeconds(1);

    private Deque<Supplier<Mono<ClientResponse>>> responses;
    private AtomicInteger                         attempts;
    private SessionQueryRevocationChecker         checker;

    @BeforeEach
    public void
    setUp()
    {
        responses = new ArrayDeque<>();
        attempts  = new AtomicInteger();

        WebClient webClient =
            WebClient
                .builder()
                .exchangeFunction(this::exchange)
                .build();

        checker = new SessionQueryRevocationChecker(webClient,LOOKUP,Duration.ofSeconds(30),TIMEOUT,Clock.systemUTC());
    }

    @Test
    public void
    aTimedOutLookupIsRetriedOnceAndThenAccepted()
    {
        responses.add(Mono::never);
        responses.add(this::live);

        assertTrue(checker.isActive(sessionToken("41")).block());
        assertEquals(2,attempts.get());
    }

    @Test
    public void
    aTransportErrorIsRetriedOnceAndThenAccepted()
    {
        responses.add(this::transportError);
        responses.add(this::live);

        assertTrue(checker.isActive(sessionToken("42")).block());
        assertEquals(2,attempts.get());
    }

    @Test
    public void
    twoTimedOutLookupsAreRejectedAndNotCached()
    {
        responses.add(Mono::never);
        responses.add(Mono::never);

        assertFalse(checker.isActive(sessionToken("43")).block());
        assertEquals(2,attempts.get());

        responses.add(this::live);

        assertTrue(checker.isActive(sessionToken("43")).block());
        assertEquals(3,attempts.get());
    }

    @Test
    public void
    twoTransportErrorsAreRejectedAndNotCachedAsActive()
    {
        responses.add(this::transportError);
        responses.add(this::transportError);

        assertFalse(checker.isActive(sessionToken("44")).block());
        assertEquals(2,attempts.get());

        responses.add(this::transportError);
        responses.add(this::transportError);

        assertFalse(checker.isActive(sessionToken("44")).block());
        assertEquals(4,attempts.get());
    }

    @Test
    public void
    aServerErrorIsRejectedWithoutRetry()
    {
        responses.add(() -> Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        assertFalse(checker.isActive(sessionToken("45")).block());
        assertEquals(1,attempts.get());
    }

    private Mono<ClientResponse>
    exchange(ClientRequest request)
    {
        attempts.incrementAndGet();

        Supplier<Mono<ClientResponse>> next = responses.poll();

        return next == null ? Mono.error(new IllegalStateException("unexpected lookup")) : next.get();
    }

    private Mono<ClientResponse>
    live()
    {
        String expires = Instant.now().plus(Duration.ofMinutes(5)).toString();

        return
            Mono.just(
                ClientResponse
                    .create(HttpStatus.OK)
                    .header("Content-Type","application/json")
                    .body("{\"success\":true,\"sessions\":[{\"expires\":\"" + expires + "\"}]}")
                    .build());
    }

    private Mono<ClientResponse>
    transportError()
    {
        return
            Mono.error(
                new WebClientRequestException(
                    new IOException("connection reset"),
                    HttpMethod.POST,
                    LOOKUP,
                    new HttpHeaders()));
    }

    private static Jwt
    sessionToken(String sessionId)
    {
        Instant now = Instant.now();

        return
            Jwt
                .withTokenValue("session-token-" + sessionId)
                .header("alg","RS256")
                .jti(sessionId)
                .subject("7")
                .claim("did","device-1")
                .issuedAt(now.minusSeconds(1))
                .expiresAt(now.plus(Duration.ofMinutes(5)))
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
