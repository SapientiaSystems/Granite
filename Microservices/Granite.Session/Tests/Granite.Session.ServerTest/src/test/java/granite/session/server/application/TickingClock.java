//////////////////////////////////////////////////////////////////////////////
// TickingClock.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

public
class TickingClock
    extends Clock
{
    private final AtomicReference<Instant> current;
    private final Duration                 tick;

    public
    TickingClock(Instant start,Duration tick)
    {
        this.current = new AtomicReference<>(start);
        this.tick    = tick;
    }

    @Override
    public ZoneId
    getZone() { return ZoneOffset.UTC; }

    @Override
    public Clock
    withZone(ZoneId zone) { return this; }

    @Override
    public Instant
    instant() { return current.getAndUpdate(value -> value.plus(tick)); }
}

//////////////////////////////////////////////////////////////////////////////
