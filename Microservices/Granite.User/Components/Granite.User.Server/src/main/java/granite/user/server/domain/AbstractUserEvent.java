//////////////////////////////////////////////////////////////////////////////
// AbstractUserEvent.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import strata.server.core.domainevent.AbstractDomainEvent;

public abstract
class AbstractUserEvent
    extends AbstractDomainEvent<User>
    implements IUserEvent
{
    protected
    AbstractUserEvent(String nm,User src)
    {
        super(nm,src);
    }

    protected
    AbstractUserEvent(String nm,String correlId,User src)
    {
        super(nm,correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
