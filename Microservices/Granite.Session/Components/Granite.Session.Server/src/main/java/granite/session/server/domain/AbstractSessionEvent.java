//////////////////////////////////////////////////////////////////////////////
// AbstractSessionEvent.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import strata.server.core.domainevent.AbstractDomainEvent;

public abstract
class AbstractSessionEvent
    extends AbstractDomainEvent<Session>
    implements ISessionEvent
{
    protected
    AbstractSessionEvent(String nm,Session src)
    {
        super(nm,src);
    }

    protected
    AbstractSessionEvent(String nm,String correlId,Session src)
    {
        super(nm,correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
