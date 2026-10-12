//////////////////////////////////////////////////////////////////////////////
// CapturingObserver.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import java.util.ArrayList;
import java.util.List;

class CapturingObserver
    implements ISessionEventObserver
{
    final List<ISessionEvent> events = new ArrayList<>();

    @Override
    public void
    onEvent(ISessionEvent event) { events.add(event); }

    @Override
    public void
    onException(Exception exc) {}
}

//////////////////////////////////////////////////////////////////////////////
