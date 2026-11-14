//////////////////////////////////////////////////////////////////////////////
// CapturingClientEventMapper.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import granite.client.server.platform.ClientEventToOutboxEventMapper;
import granite.client.server.platform.IClientEventToOutboxEventMapper;
import granite.client.service.event.ClientEvent;
import strata.outbox.core.repository.OutboxEvent;
import strata.outbox.core.shared.MappingException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public
class CapturingClientEventMapper
    implements IClientEventToOutboxEventMapper
{
    public static final List<ClientEvent> EVENTS   = new CopyOnWriteArrayList<>();
    public static final List<String>      PAYLOADS = new CopyOnWriteArrayList<>();

    private final IClientEventToOutboxEventMapper delegate = new ClientEventToOutboxEventMapper();

    @Override
    public OutboxEvent
    map(ClientEvent event)
        throws MappingException
    {
        OutboxEvent mapped = delegate.map(event);

        EVENTS.add(event);
        PAYLOADS.add(mapped.getEventPayload());
        return mapped;
    }
}

//////////////////////////////////////////////////////////////////////////////
