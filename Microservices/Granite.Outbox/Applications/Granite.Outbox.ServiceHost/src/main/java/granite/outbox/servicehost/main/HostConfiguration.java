//////////////////////////////////////////////////////////////////////////////
// HostConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.servicehost.main;

import granite.feedback.service.event.FeedbackEvent;
import granite.outbox.server.feedback.FeedbackEventOutboxEventReceiver;
import granite.outbox.server.feedback.KafkaAvroFeedbackEventSender;
import granite.user.service.event.UserEvent;
import granite.outbox.server.configuration.GraniteOutboxSecurityConfiguration;
import granite.outbox.server.user.KafkaAvroUserEventSender;
import granite.outbox.server.user.UserEventOutboxEventReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;
import strata.foundation.kafka.event.KafkaConfigurationProvider;
import strata.foundation.spring.inject.SpringConfigurationProvider;
import strata.outbox.core.receiver.IOutboxEventReceiverMapProvider;
import strata.outbox.server.configuration.StrataPollingOutboxServerConfiguration;
import strata.server.spring.health.HealthCheckConfiguration;

import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableAsync
@Import({HealthCheckConfiguration.class,GraniteOutboxSecurityConfiguration.class})
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
                            "granite.userevent.avro")),
                    FeedbackEvent
                        .class
                        .getSimpleName(),
                    new FeedbackEventOutboxEventReceiver(
                        new KafkaAvroFeedbackEventSender(
                            provider,
                            "granite.feedbackevent.avro")));
    }

    @Bean
    @Scope("singleton")
    public IConfiguration
    configuration(Environment environment)
    {
        return
            new SpringConfigurationProvider(environment).get();
    }

    @Bean
    public CommonsRequestLoggingFilter
    requestLoggingFilter()
    {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}

//////////////////////////////////////////////////////////////////////////////
