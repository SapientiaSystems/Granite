//////////////////////////////////////////////////////////////////////////////
// ServiceTokenProviderTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CommitStage")
public
class ServiceTokenProviderTest
{
    private static final Instant  NOW      = Instant.parse("2026-10-20T12:00:00Z");
    private static final Duration LIFETIME = Duration.ofMinutes(5);
    private static final String   SUBJECT  = "service:granite-session";

    private IJwtTokenService     jwtTokenService;
    private Clock                clock;
    private ServiceTokenProvider subject;

    @BeforeEach
    public void
    setUp()
    {
        jwtTokenService = Mockito.mock(IJwtTokenService.class);
        clock           = Mockito.mock(Clock.class);
        subject         =
            new ServiceTokenProvider(jwtTokenService,clock,SUBJECT,List.of("SCOPE_internal"),LIFETIME);

        Mockito
            .when(jwtTokenService.mintServiceToken(SUBJECT,List.of("SCOPE_internal"),LIFETIME))
            .thenReturn("token-1","token-2");
    }

    @Test
    public void
    reusesTokenUntilRenewalMargin()
    {
        Mockito.when(clock.instant()).thenReturn(NOW,NOW.plus(Duration.ofMinutes(3)));

        assertEquals("token-1",subject.getToken());
        assertEquals("token-1",subject.getToken());
        Mockito
            .verify(jwtTokenService,Mockito.times(1))
            .mintServiceToken(SUBJECT,List.of("SCOPE_internal"),LIFETIME);
    }

    @Test
    public void
    renewsTokenWithinOneMinuteOfExpiry()
    {
        Mockito.when(clock.instant()).thenReturn(NOW,NOW.plus(Duration.ofMinutes(4)));

        assertEquals("token-1",subject.getToken());
        assertEquals("token-2",subject.getToken());
    }

    @Test
    public void
    rejectsLifetimeShorterThanTwoRenewalMargins()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new ServiceTokenProvider(jwtTokenService,clock,SUBJECT,List.of(),Duration.ofSeconds(90)));
    }
}

//////////////////////////////////////////////////////////////////////////////
