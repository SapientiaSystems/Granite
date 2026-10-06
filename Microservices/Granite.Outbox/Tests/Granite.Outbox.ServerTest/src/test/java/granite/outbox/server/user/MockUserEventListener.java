//////////////////////////////////////////////////////////////////////////////
// MockUserEventListener.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.user;

import granite.user.service.event.IUserEventListener;
import granite.user.service.event.UserEvent;
import strata.foundation.core.event.IteratedEventListener;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public
class MockUserEventListener
    extends IteratedEventListener<UserEvent>
    implements IUserEventListener
{
    private final List<UserEvent> received;

    public MockUserEventListener()
    {
        this.received = new ArrayList<>();
    }

    @Override
    public void
    onEvent(UserEvent event)
    {
        received.add(event);
        System
            .out
            .println("onEvent(" + event + ") called");
    }

    @Override
    public void
    onException(Exception e)
    {
        System
            .out
            .println("onException(" + e + ") called");
    }

    public void
    assertReceivedAtLeast(int i)
    {
        assertTrue(received.size() >= i);
    }

    public void
    assertReceived(UserEvent expected)
    {
        UserEvent actual =
            received
                .stream()
                .filter(e -> e.getIdentifiers().getEventId().equals(expected.getIdentifiers().getEventId()))
                .findFirst()
                .orElseThrow();

        assertEventsEquals(expected,actual);
    }

    public void
    assertLastReceived(UserEvent expected)
    {
        UserEvent actual = received.getLast();

        assertEventsEquals(expected,actual);
    }

    public void
    assertReceivedAtLeast(UserEvent expected,int index)
    {
        UserEvent actual = received.get(index);

        assertEventsEquals(expected,actual);
    }

    public void
    assertEventsEquals(UserEvent expected,UserEvent actual)
    {
        assertEquals(expected.getEventType(),actual.getEventType());
        assertEquals(expected.getSourceType(),actual.getSourceType());
        assertEquals(expected.getSourceId(),actual.getSourceId());
        assertEquals(
            expected
                .getIdentifiers()
                .getEventId(),
            actual
                .getIdentifiers()
                .getEventId());
        assertEquals(
            expected
                .getIdentifiers()
                .getCorrelationId(),
            actual
                .getIdentifiers()
                .getCorrelationId());
        assertEquals(
            expected
                .getIdentifiers()
                .getTimestamp(),
            actual
                .getIdentifiers()
                .getTimestamp());
    }
}

//////////////////////////////////////////////////////////////////////////////
