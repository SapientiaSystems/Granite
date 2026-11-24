//////////////////////////////////////////////////////////////////////////////
// ClientEventRelayTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.client;

import granite.client.service.event.ClientEvent;
import granite.client.service.event.IClientEventSender;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import strata.foundation.core.event.EventIdentifiersData;
import strata.foundation.core.event.CompletableSendResult;
import strata.foundation.core.event.SendResult;
import strata.foundation.core.event.StandardEventType;
import strata.outbox.core.repository.OutboxEvent;
import strata.outbox.core.shared.ObjectMapperProvider;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("CommitStage")
public
class ClientEventRelayTest
{
    @Test
    public void
    clientEventIsRelayedToClientTopic() throws Exception
    {
        IClientEventSender sender = mock(IClientEventSender.class);
        ClientEvent expected =
            ClientEvent
                .newBuilder()
                .setIdentifiers(
                    EventIdentifiersData
                        .newBuilder()
                        .setEventId("test-event-id")
                        .setCorrelationId("test-correlation-id")
                        .setTimestamp(Instant.now())
                        .build())
                .setEventType(StandardEventType.CREATED)
                .setSourceType("granite.client.server.domain.Client")
                .setSourceId(42L)
                .build();

        when(sender.send(any()))
            .thenReturn(
                new CompletableSendResult<>(
                    CompletableFuture.completedFuture(new SendResult<>(expected))));

        ClientEventOutboxEventReceiver receiver =
            new ClientEventOutboxEventReceiver(sender);

        receiver.receive(
            new OutboxEvent()
                .setSourceType(ClientEvent.class.getTypeName())
                .setSourceId("42")
                .setEventType(ClientEvent.class.getSimpleName())
                .setEventPayload(
                    new ObjectMapperProvider()
                        .get()
                        .writeValueAsString(expected)));

        ArgumentCaptor<ClientEvent> captor =
            ArgumentCaptor.forClass(ClientEvent.class);

        verify(sender).send(captor.capture());
        assertEquals(expected.getSourceId(),captor.getValue().getSourceId());
        assertEquals(
            expected.getIdentifiers().getEventId(),
            captor.getValue().getIdentifiers().getEventId());
    }
}

//////////////////////////////////////////////////////////////////////////////
