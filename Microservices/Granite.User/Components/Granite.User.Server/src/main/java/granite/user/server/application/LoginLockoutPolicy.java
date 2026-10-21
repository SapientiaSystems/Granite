//////////////////////////////////////////////////////////////////////////////
// LoginLockoutPolicy.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public
class LoginLockoutPolicy
{
    private final int      maxAttempts;
    private final Duration lockDuration;
    private final Clock    clock;

    public
    LoginLockoutPolicy(int maxAttempts,Duration lockDuration,Clock clock)
    {
        if (maxAttempts < 1)
            throw new IllegalArgumentException("maxAttempts must be at least 1");

        this.maxAttempts  = maxAttempts;
        this.lockDuration = Objects.requireNonNull(lockDuration,"lockDuration");
        this.clock        = Objects.requireNonNull(clock,"clock");
    }

    public int
    getMaxAttempts() { return maxAttempts; }

    public Duration
    getLockDuration() { return lockDuration; }

    public Instant
    now() { return clock.instant(); }

    public static LoginLockoutPolicy
    defaults()
    {
        return new LoginLockoutPolicy(5,Duration.ofMinutes(15),Clock.systemUTC());
    }
}

//////////////////////////////////////////////////////////////////////////////
