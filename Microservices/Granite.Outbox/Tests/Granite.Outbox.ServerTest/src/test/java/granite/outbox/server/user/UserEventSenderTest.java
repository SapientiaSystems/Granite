//////////////////////////////////////////////////////////////////////////////
// UserEventSenderTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.user;

import granite.user.client.event.UserKafkaAvroEventReceiver;
import granite.user.service.event.IUserEventReceiver;
import granite.user.service.event.IUserEventSender;
import granite.user.service.event.UserEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import strata.foundation.core.event.EventIdentifiersData;
import strata.foundation.core.event.StandardEventType;
import strata.foundation.core.inject.ApplicationConfigurationProvider;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;
import strata.foundation.kafka.event.KafkaConfigurationProvider;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static granite.outbox.server.shared.TestHelper.sleepForSeconds;

@Tag("NotReadyYet")
public
class UserEventSenderTest
{
    private IUserEventSender      sender;
    private IUserEventReceiver    receiver;
    private MockUserEventListener listener;

    @BeforeEach
    public void
    setup() throws Exception
    {
        IKafkaConfigurationProvider provider =
            new KafkaConfigurationProvider(
                new ApplicationConfigurationProvider("application-local").get());

        sender = new KafkaAvroUserEventSender(provider,"test-granite.userevent.avro");
        receiver = new UserKafkaAvroEventReceiver(provider,"test-granite.userevent.avro");
        listener = new MockUserEventListener();

        sender.open();
        receiver.startListening(listener);
    }

    @AfterEach
    public void
    tearDown() throws Exception
    {
        receiver.stopListening();
        sender.close();
        receiver = null;
        sender = null;
        listener = null;
    }

    @Test
    public void
    testSendUserEvent()
    {
        UserEvent expected =
            UserEvent
                .newBuilder()
                .setIdentifiers(
                    EventIdentifiersData
                        .newBuilder()
                        .setEventId("test-event-id")
                        .setCorrelationId("test-correlation-id")
                        .setTimestamp(Instant.now())
                        .build())
                .setEventType(StandardEventType.CREATED)
                .setSourceType("granite.user.server.domain.User")
                .setSourceId(123456789L)
                .build();

        sender
            .send(expected)
            .thenAccept(result -> assertTrue(result.isSuccess()));

        sleepForSeconds(10);

        listener.assertReceivedAtLeast(1);
        listener.assertReceived(expected);
    }
}

//////////////////////////////////////////////////////////////////////////////
