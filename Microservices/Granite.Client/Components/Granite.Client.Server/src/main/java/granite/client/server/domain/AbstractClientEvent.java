//////////////////////////////////////////////////////////////////////////////
// AbstractClientEvent.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.domain;

import strata.server.core.domainevent.AbstractDomainEvent;

public abstract
class AbstractClientEvent
    extends AbstractDomainEvent<Client>
    implements IClientEvent
{
    protected
    AbstractClientEvent(String nm,Client src)
    {
        super(nm,src);
    }

    protected
    AbstractClientEvent(String nm,String correlId,Client src)
    {
        super(nm,correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
