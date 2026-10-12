//////////////////////////////////////////////////////////////////////////////
// MockSessionEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.service.event.ISessionEventSender;
import granite.session.service.event.SessionEvent;
import strata.foundation.core.event.CompletableSendResult;
import strata.foundation.core.event.ICompletableSendResult;
import strata.foundation.core.event.IEventSender;
import strata.foundation.core.event.SendResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public
class MockSessionEventSender
    implements ISessionEventSender
{
    private final List<SessionEvent> events;
    private       boolean            open;

    public
    MockSessionEventSender()
    {
        events = new ArrayList<>();
        open   = false;
    }

    @Override
    public IEventSender<SessionEvent>
    open()
    {
        open = true;
        return this;
    }

    @Override
    public IEventSender<SessionEvent>
    close()
    {
        open = false;
        return this;
    }

    @Override
    public ICompletableSendResult<SessionEvent>
    send(SessionEvent event)
    {
        events.add(event);
        return
            new CompletableSendResult<>(
                CompletableFuture.completedFuture(new SendResult<>(event)));
    }

    @Override
    public boolean
    isOpen() { return open; }

    @Override
    public boolean
    isClosed() { return !open; }

    public List<SessionEvent>
    getEvents() { return Collections.unmodifiableList(events); }

    public void
    clear() { events.clear(); }
}

//////////////////////////////////////////////////////////////////////////////
