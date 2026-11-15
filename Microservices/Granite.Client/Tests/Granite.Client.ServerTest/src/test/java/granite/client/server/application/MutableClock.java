//////////////////////////////////////////////////////////////////////////////
// MutableClock.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public
class MutableClock
    extends Clock
{
    private volatile Instant now;

    public
    MutableClock(Instant start)
    {
        now = start;
    }

    public MutableClock
    set(Instant value)
    {
        now = value;
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
