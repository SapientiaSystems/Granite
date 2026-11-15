//////////////////////////////////////////////////////////////////////////////
// TestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.client.server.configuration.GraniteClientCommandServerConfiguration;
import granite.client.service.event.IClientEventSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;
import strata.foundation.spring.inject.RequestScoped;
import strata.foundation.spring.inject.SingletonScoped;
import strata.outbox.core.repository.IOutboxEventRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

@Configuration
@PropertySource({"classpath:application.properties","classpath:application-local.properties"})
public
class TestConfiguration
    extends GraniteClientCommandServerConfiguration
{
    public static final MutableClock CLOCK = new MutableClock(Instant.parse("2026-11-14T12:00:00Z"));

    @Override
    @Bean
    @SingletonScoped
    public IConfiguration
    configuration(Environment environment)
    {
        return new ApplicationConfigurationProvider("application-local").get();
    }

    @Override
    @Bean
    @SingletonScoped
    public Clock
    clock()
    {
        return CLOCK;
    }

    @Override
    @Bean
    @SingletonScoped
    public Supplier<Optional<String>>
    clientCallerSubject()
    {
        return new TestCallerSubject();
    }

    @Override
    @Bean
    @RequestScoped
    public IClientEventSender
    clientEventSender(IOutboxEventRepository repository)
    {
        return new CapturingClientEventSender(repository);
    }

    @Bean
    @SingletonScoped
    public ObjectMapper
    objectMapper()
    {
        return new ObjectMapper();
    }
}

//////////////////////////////////////////////////////////////////////////////
