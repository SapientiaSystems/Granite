//////////////////////////////////////////////////////////////////////////////
// FixedWindowRateLimiter.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public
class FixedWindowRateLimiter
{
    private final int                          limit;
    private final Duration                     window;
    private final int                          maxClients;
    private final Clock                        clock;
    private final Map<String,FixedWindowCount> counts;

    public
    FixedWindowRateLimiter(int limit,Duration window,int maxClients,Clock clock)
    {
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be positive");

        if (window == null || window.isZero() || window.isNegative())
            throw new IllegalArgumentException("window must be positive");

        if (maxClients <= 0)
            throw new IllegalArgumentException("maxClients must be positive");

        this.limit      = limit;
        this.window     = window;
        this.maxClients = maxClients;
        this.clock      = Objects.requireNonNull(clock,"clock");
        this.counts     = new ConcurrentHashMap<>();
    }

    public boolean
    tryAcquire(String client)
    {
        Instant now = clock.instant();

        if (!counts.containsKey(client) && counts.size() >= maxClients)
        {
            evictExpired(now);

            if (counts.size() >= maxClients)
                return false;
        }

        FixedWindowCount count =
            counts.compute(
                client,
                (key,current) ->
                    current == null || isExpired(current,now)
                        ? new FixedWindowCount(now,1)
                        : current.count() > limit ? current : current.increment());

        return count.count() <= limit;
    }

    public int
    getTrackedClients()
    {
        return counts.size();
    }

    private void
    evictExpired(Instant now)
    {
        counts.values().removeIf(count -> isExpired(count,now));
    }

    private boolean
    isExpired(FixedWindowCount count,Instant now)
    {
        return !now.isBefore(count.start().plus(window));
    }
}

//////////////////////////////////////////////////////////////////////////////
