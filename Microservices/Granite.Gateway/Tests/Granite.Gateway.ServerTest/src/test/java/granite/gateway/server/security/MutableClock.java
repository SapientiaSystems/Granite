//////////////////////////////////////////////////////////////////////////////
// MutableClock.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public
class MutableClock
    extends Clock
{
    private Instant now;

    public
    MutableClock(Instant start)
    {
        now = start;
    }

    public MutableClock
    advance(Duration duration)
    {
        now = now.plus(duration);
        return this;
    }

    @Override
    public ZoneId
    getZone() { return ZoneOffset.UTC; }

    @Override
    public Clock
    withZone(ZoneId zone) { return this; }

    @Override
    public Instant
    instant() { return now; }
}

//////////////////////////////////////////////////////////////////////////////
