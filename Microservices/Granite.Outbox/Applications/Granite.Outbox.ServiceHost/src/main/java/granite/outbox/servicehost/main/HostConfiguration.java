//////////////////////////////////////////////////////////////////////////////
// HostConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.servicehost.main;

import granite.user.service.event.UserEvent;
import granite.outbox.server.user.KafkaAvroUserEventSender;
import granite.outbox.server.user.UserEventOutboxEventReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;
import strata.foundation.kafka.event.KafkaConfigurationProvider;
import strata.outbox.core.receiver.IOutboxEventReceiverMapProvider;
import strata.outbox.server.configuration.StrataChangeCaptureOutboxServerConfiguration;
import strata.outbox.server.configuration.StrataPollingOutboxServerConfiguration;
import strata.server.spring.health.HealthCheckConfiguration;

import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableAsync
@Import({HealthCheckConfiguration.class})
public
class HostConfiguration
    extends StrataPollingOutboxServerConfiguration
{
    @Override
    @Bean
    public IOutboxEventReceiverMapProvider
    receiverMapProvider(IConfiguration configuration)
    {
        IKafkaConfigurationProvider provider =
            new KafkaConfigurationProvider(configuration);

        return
            () ->
                Map.of(
                    UserEvent
                        .class
                        .getSimpleName(),
                    new UserEventOutboxEventReceiver(
                        new KafkaAvroUserEventSender(
                            provider,
                            "granite.userevent.avro")));
    }

    @Bean
    @Scope("singleton")
    public IConfiguration
    configuration()
    {
        return
            new ApplicationConfigurationProvider("development").get();
    }

    @Bean
    public CommonsRequestLoggingFilter
    requestLoggingFilter()
    {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}

//////////////////////////////////////////////////////////////////////////////
