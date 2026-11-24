//////////////////////////////////////////////////////////////////////////////
// ExtendedSessionCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import granite.session.server.platform.PlatformContextTestConfiguration;
import granite.session.service.event.ISessionEventSender;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import strata.foundation.spring.inject.RequestScoped;
import strata.foundation.spring.inject.SingletonScoped;
import strata.outbox.core.repository.IOutboxEventRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@Configuration
public
class ExtendedSessionCommandServerConfiguration
    extends PlatformContextTestConfiguration
{
    public static final Instant FIXED_INSTANT = Instant.parse("2026-01-01T00:00:00Z");

    @Override
    @Bean
    @RequestScoped
    public ISessionEventSender
    sessionEventSender(IOutboxEventRepository repository)
    {
        return Mockito.mock(ISessionEventSender.class);
    }

    @Override
    @Bean
    @SingletonScoped
    public Clock
    clock()
    {
        return Clock.fixed(FIXED_INSTANT,ZoneOffset.UTC);
    }

    @Override
    protected ModelMapper
    configureMapper(ModelMapper mapper)
    {
        super.configureMapper(mapper);
        mapper.getConfiguration().setSkipNullEnabled(true);
        return mapper;
    }
}

//////////////////////////////////////////////////////////////////////////////
