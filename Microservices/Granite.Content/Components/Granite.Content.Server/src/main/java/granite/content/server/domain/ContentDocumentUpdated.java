//////////////////////////////////////////////////////////////////////////////
// ContentDocumentUpdated.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

public
class ContentDocumentUpdated
    extends AbstractContentDocumentEvent
{
    public
    ContentDocumentUpdated(ContentDocument src)
    {
        super("ContentDocumentUpdated",src);
    }

    public
    ContentDocumentUpdated(String correlId,ContentDocument src)
    {
        super("ContentDocumentUpdated",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
