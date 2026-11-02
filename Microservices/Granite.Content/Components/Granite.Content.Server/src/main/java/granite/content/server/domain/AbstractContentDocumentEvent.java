//////////////////////////////////////////////////////////////////////////////
// AbstractContentDocumentEvent.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

import strata.server.core.domainevent.AbstractDomainEvent;

public abstract
class AbstractContentDocumentEvent
    extends AbstractDomainEvent<ContentDocument>
    implements IContentDocumentEvent
{
    protected
    AbstractContentDocumentEvent(String nm,ContentDocument src)
    {
        super(nm,src);
    }

    protected
    AbstractContentDocumentEvent(String nm,String correlId,ContentDocument src)
    {
        super(nm,correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
