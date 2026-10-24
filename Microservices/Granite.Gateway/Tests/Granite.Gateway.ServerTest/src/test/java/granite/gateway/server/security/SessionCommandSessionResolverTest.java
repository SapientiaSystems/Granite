//////////////////////////////////////////////////////////////////////////////
// SessionCommandSessionResolverTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import granite.gateway.server.configuration.GatewayTestBackend;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionCommandSessionResolverTest
{
    private static GatewayTestBackend backend;

    private MutableClock clock;

    @BeforeAll
    public static void
    startBackend()
        throws Exception
    {
        backend = new GatewayTestBackend();
    }

    @AfterAll
    public static void
    stopBackend()
    {
        backend.stop();
    }

    @BeforeEach
    public void
    setUp()
        throws Exception
    {
        clock = new MutableClock(Instant.now());
    }

    @Test
    public void
    resolvesHandleWithoutAuthorizationHeader()
        throws Exception
    {
        String handle = newHandle();
        String token  = registerSession(handle,Duration.ofMinutes(5));

        assertEquals(token,resolver(Duration.ofSeconds(60)).resolve(handle).block());
        assertTrue(backend.getResolveCalls().contains(handle));
        assertFalse(backend.getResolveAuthorizations().isEmpty());
        assertTrue(backend.getResolveAuthorizations().stream().allMatch("null"::equals));
    }

    @Test
    public void
    cachesForConfiguredTtl()
        throws Exception
    {
        String                         handle   = newHandle();
        String                         token    = registerSession(handle,Duration.ofMinutes(5));
        SessionCommandSessionResolver  resolver = resolver(Duration.ofSeconds(60));

        assertEquals(token,resolver.resolve(handle).block());
        assertEquals(token,resolver.resolve(handle).block());
        assertEquals(1,calls(handle));

        clock.advance(Duration.ofSeconds(61));
        assertEquals(token,resolver.resolve(handle).block());
        assertEquals(2,calls(handle));
    }

    @Test
    public void
    cacheIsCappedByTokenExpiry()
        throws Exception
    {
        String                         handle   = newHandle();
        SessionCommandSessionResolver  resolver = resolver(Duration.ofSeconds(60));

        registerSession(handle,Duration.ofSeconds(10));
        resolver.resolve(handle).block();
        clock.advance(Duration.ofSeconds(5));
        resolver.resolve(handle).block();
        assertEquals(1,calls(handle));

        clock.advance(Duration.ofSeconds(6));
        resolver.resolve(handle).block();
        assertEquals(2,calls(handle));
    }

    @Test
    public void
    cacheKeyIsHashOfHandle()
        throws Exception
    {
        String                         handle   = newHandle();
        SessionCommandSessionResolver  resolver = resolver(Duration.ofSeconds(60));

        registerSession(handle,Duration.ofMinutes(5));
        resolver.resolve(handle).block();

        assertEquals(Set.of(sha256(handle)),resolver.getCacheKeys());
    }

    @Test
    public void
    unknownHandleIsEmptyAndNotCached()
    {
        String                         handle   = newHandle();
        SessionCommandSessionResolver  resolver = resolver(Duration.ofSeconds(60));

        assertNull(resolver.resolve(handle).block());
        assertNull(resolver.resolve(handle).block());
        assertEquals(2,calls(handle));
        assertTrue(resolver.getCacheKeys().isEmpty());
    }

    @Test
    public void
    backendErrorIsEmptyAndNotCached()
        throws Exception
    {
        String                         handle   = newHandle();
        SessionCommandSessionResolver  resolver = resolver(Duration.ofSeconds(60));

        registerSession(handle,Duration.ofMinutes(5));
        backend.failResolvesFor(handle);

        assertNull(resolver.resolve(handle).block());
        assertNull(resolver.resolve(handle).block());
        assertEquals(2,calls(handle));
    }

    @Test
    public void
    timeoutIsEmpty()
        throws Exception
    {
        String handle = newHandle();

        registerSession(handle,Duration.ofMinutes(5));
        backend.delayResolvesFor(handle);

        SessionCommandSessionResolver resolver =
            new SessionCommandSessionResolver(
                WebClient.create(),
                resolveUri(),
                Duration.ofSeconds(60),
                Duration.ofMillis(300),
                clock);

        assertNull(resolver.resolve(handle).block(Duration.ofSeconds(2)));
    }

    private SessionCommandSessionResolver
    resolver(Duration ttl)
    {
        return
            new SessionCommandSessionResolver(
                WebClient.create(),
                resolveUri(),
                ttl,
                Duration.ofSeconds(2),
                clock);
    }

    private static URI
    resolveUri()
    {
        return URI.create(backend.getBaseUrl() + "/session-command-service/resolve-session");
    }

    private static String
    registerSession(String handle,Duration lifetime)
        throws Exception
    {
        String token = backend.sign(backend.newSessionId(),List.of("ROLE_USER"),lifetime);

        backend.registerHandle(handle,token);
        return token;
    }

    private static int
    calls(String handle)
    {
        return Collections.frequency(backend.getResolveCalls(),handle);
    }

    private static String
    newHandle()
    {
        return UUID.randomUUID().toString().replace("-","") + "handle";
    }

    private static String
    sha256(String value)
        throws Exception
    {
        return
            HexFormat
                .of()
                .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}

//////////////////////////////////////////////////////////////////////////////
