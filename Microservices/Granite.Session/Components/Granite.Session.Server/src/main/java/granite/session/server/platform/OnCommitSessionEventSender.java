//////////////////////////////////////////////////////////////////////////////
// OnCommitSessionEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.service.event.ISessionEventSender;
import granite.session.service.event.SessionEvent;
import jakarta.inject.Inject;
import strata.foundation.core.event.IEventSender;
import strata.server.core.event.OnCommitEventSender;
import strata.server.core.unitofwork.IUnitOfWorkSynchronizationManager;

public
class OnCommitSessionEventSender
    extends OnCommitEventSender<SessionEvent>
    implements ISessionEventSender
{
    @Inject
    public
    OnCommitSessionEventSender(
        IEventSender<SessionEvent>        imp,
        IUnitOfWorkSynchronizationManager mgr)
    {
        super(imp,mgr);
    }
}

//////////////////////////////////////////////////////////////////////////////
