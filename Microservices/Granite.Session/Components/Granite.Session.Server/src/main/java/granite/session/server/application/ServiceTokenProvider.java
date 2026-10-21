//////////////////////////////////////////////////////////////////////////////
// ServiceTokenProvider.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public
class ServiceTokenProvider
    implements IServiceTokenProvider
{
    private static final Duration RENEWAL_MARGIN = Duration.ofSeconds(60);

    private final IJwtTokenService jwtTokenService;
    private final Clock            clock;
    private final String           subject;
    private final List<String>     authorities;
    private final Duration         lifetime;
    private String                 token;
    private Instant                renewAt;

    public
    ServiceTokenProvider(
        IJwtTokenService jwtTokenService,
        Clock            clock,
        String           subject,
        List<String>     authorities,
        Duration         lifetime)
    {
        this.jwtTokenService = Objects.requireNonNull(jwtTokenService,"jwtTokenService");
        this.clock           = Objects.requireNonNull(clock,"clock");
        this.subject         = Objects.requireNonNull(subject,"subject");
        this.authorities     = List.copyOf(authorities);
        this.lifetime        = Objects.requireNonNull(lifetime,"lifetime");
        this.token           = null;
        this.renewAt         = null;

        if (lifetime.compareTo(RENEWAL_MARGIN.multipliedBy(2)) < 0)
            throw new IllegalArgumentException("lifetime must be at least " + RENEWAL_MARGIN.multipliedBy(2));
    }

    @Override
    public synchronized String
    getToken()
    {
        Instant now = clock.instant();

        if (token == null || !now.isBefore(renewAt))
        {
            token   = jwtTokenService.mintServiceToken(subject,authorities,lifetime);
            renewAt = now.plus(lifetime).minus(RENEWAL_MARGIN);
        }

        return token;
    }
}

//////////////////////////////////////////////////////////////////////////////
