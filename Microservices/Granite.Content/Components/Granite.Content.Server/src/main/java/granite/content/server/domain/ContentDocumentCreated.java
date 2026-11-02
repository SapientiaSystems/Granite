//////////////////////////////////////////////////////////////////////////////
// ContentDocumentCreated.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

public
class ContentDocumentCreated
    extends AbstractContentDocumentEvent
{
    public
    ContentDocumentCreated(ContentDocument src)
    {
        super("ContentDocumentCreated",src);
    }

    public
    ContentDocumentCreated(String correlId,ContentDocument src)
    {
        super("ContentDocumentCreated",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
