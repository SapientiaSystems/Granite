//////////////////////////////////////////////////////////////////////////////
// FixedWindowRateLimiterTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class FixedWindowRateLimiterTest
{
    @Test
    public void
    allowsTheLimitPerWindowThenRefuses()
    {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(3,Duration.ofMinutes(1),100,clock());

        assertTrue(limiter.tryAcquire("10.0.0.1"));
        assertTrue(limiter.tryAcquire("10.0.0.1"));
        assertTrue(limiter.tryAcquire("10.0.0.1"));
        assertFalse(limiter.tryAcquire("10.0.0.1"));
        assertFalse(limiter.tryAcquire("10.0.0.1"));
    }

    @Test
    public void
    clientsAreCountedSeparately()
    {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(1,Duration.ofMinutes(1),100,clock());

        assertTrue(limiter.tryAcquire("10.0.0.1"));
        assertFalse(limiter.tryAcquire("10.0.0.1"));
        assertTrue(limiter.tryAcquire("10.0.0.2"));
    }

    @Test
    public void
    nextWindowStartsAfresh()
    {
        MutableClock           clock   = clock();
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(1,Duration.ofMinutes(1),100,clock);

        assertTrue(limiter.tryAcquire("10.0.0.1"));
        assertFalse(limiter.tryAcquire("10.0.0.1"));
        clock.advance(Duration.ofSeconds(59));
        assertFalse(limiter.tryAcquire("10.0.0.1"));
        clock.advance(Duration.ofSeconds(1));
        assertTrue(limiter.tryAcquire("10.0.0.1"));
    }

    @Test
    public void
    trackedClientsAreBoundedAndExpiredWindowsAreEvicted()
    {
        MutableClock           clock   = clock();
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(5,Duration.ofMinutes(1),2,clock);

        assertTrue(limiter.tryAcquire("10.0.0.1"));
        assertTrue(limiter.tryAcquire("10.0.0.2"));
        assertFalse(limiter.tryAcquire("10.0.0.3"));
        assertTrue(limiter.tryAcquire("10.0.0.1"));
        assertEquals(2,limiter.getTrackedClients());

        clock.advance(Duration.ofMinutes(1));

        assertTrue(limiter.tryAcquire("10.0.0.3"));
        assertEquals(1,limiter.getTrackedClients());
    }

    @Test
    public void
    rejectsNonPositiveSettings()
    {
        assertThrows(IllegalArgumentException.class,() -> new FixedWindowRateLimiter(0,Duration.ofMinutes(1),1,clock()));
        assertThrows(IllegalArgumentException.class,() -> new FixedWindowRateLimiter(1,Duration.ZERO,1,clock()));
        assertThrows(IllegalArgumentException.class,() -> new FixedWindowRateLimiter(1,Duration.ofMinutes(1),0,clock()));
    }

    private static MutableClock
    clock()
    {
        return new MutableClock(Instant.parse("2026-10-28T12:00:00Z"));
    }
}

//////////////////////////////////////////////////////////////////////////////
