//////////////////////////////////////////////////////////////////////////////
// ContentDocumentDestroyed.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

public
class ContentDocumentDestroyed
    extends AbstractContentDocumentEvent
{
    public
    ContentDocumentDestroyed(ContentDocument src)
    {
        super("ContentDocumentDestroyed",src);
    }

    public
    ContentDocumentDestroyed(String correlId,ContentDocument src)
    {
        super("ContentDocumentDestroyed",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
