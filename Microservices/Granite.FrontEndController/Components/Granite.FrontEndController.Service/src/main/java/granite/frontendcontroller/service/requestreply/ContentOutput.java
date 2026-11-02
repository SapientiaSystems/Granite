//////////////////////////////////////////////////////////////////////////////
// ContentOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.content.service.requestreply.ContentDocumentData;
import granite.content.service.requestreply.ContentHitData;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import java.util.ArrayList;
import java.util.List;

public
class ContentOutput
    extends FrontEndOutput
{
    private List<ContentHitData> index;
    private List<ContentHitData> hits;
    private ContentDocumentData  article;
    private List<String>         failureMessages;

    public
    ContentOutput()
    {
        super();
        index           = new ArrayList<>();
        hits            = new ArrayList<>();
        article         = null;
        failureMessages = new ArrayList<>();
    }

    @Override
    public ContentOutput
    setUserId(Long id)
    {
        return (ContentOutput)super.setUserId(id);
    }

    @Override
    public ContentOutput
    setSource(String src)
    {
        return (ContentOutput)super.setSource(src);
    }

    @Override
    public ContentOutput
    setDestination(String dest)
    {
        return (ContentOutput)super.setDestination(dest);
    }

    @Override
    public ContentOutput
    setCommand(String cmd)
    {
        return (ContentOutput)super.setCommand(cmd);
    }

    public ContentOutput
    setIndex(List<ContentHitData> value)
    {
        index = value;
        return this;
    }

    public ContentOutput
    setHits(List<ContentHitData> value)
    {
        hits = value;
        return this;
    }

    public ContentOutput
    setArticle(ContentDocumentData value)
    {
        article = value;
        return this;
    }

    public ContentOutput
    setFailureMessages(List<String> value)
    {
        failureMessages = value;
        return this;
    }

    public List<ContentHitData>
    getIndex() { return index; }

    public List<ContentHitData>
    getHits() { return hits; }

    public ContentDocumentData
    getArticle() { return article; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
