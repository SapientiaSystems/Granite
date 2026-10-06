//////////////////////////////////////////////////////////////////////////////
// OnCommitUserEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.service.event.IUserEventSender;
import granite.user.service.event.UserEvent;
import jakarta.inject.Inject;
import strata.foundation.core.event.IEventSender;
import strata.server.core.event.OnCommitEventSender;
import strata.server.core.unitofwork.IUnitOfWorkSynchronizationManager;

public
class OnCommitUserEventSender
    extends OnCommitEventSender<UserEvent>
    implements IUserEventSender
{
    @Inject
    public
    OnCommitUserEventSender(
        IEventSender<UserEvent>     imp,
        IUnitOfWorkSynchronizationManager mgr)
    {
        super(imp,mgr);
    }
}

//////////////////////////////////////////////////////////////////////////////
