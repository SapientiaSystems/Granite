//////////////////////////////////////////////////////////////////////////////
// AbstractConversationEvent.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.domain;

import strata.server.core.domainevent.AbstractDomainEvent;

public abstract
class AbstractConversationEvent
    extends AbstractDomainEvent<Conversation>
    implements IConversationEvent
{
    protected
    AbstractConversationEvent(String nm,Conversation src)
    {
        super(nm,src);
    }

    protected
    AbstractConversationEvent(String nm,String correlId,Conversation src)
    {
        super(nm,correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
