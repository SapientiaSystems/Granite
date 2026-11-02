//////////////////////////////////////////////////////////////////////////////
// AbstractFeedbackEvent.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.domain;

import strata.server.core.domainevent.AbstractDomainEvent;

public abstract
class AbstractFeedbackEvent
    extends AbstractDomainEvent<Feedback>
    implements IFeedbackEvent
{
    protected
    AbstractFeedbackEvent(String nm,Feedback src)
    {
        super(nm,src);
    }

    protected
    AbstractFeedbackEvent(String nm,String correlId,Feedback src)
    {
        super(nm,correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
