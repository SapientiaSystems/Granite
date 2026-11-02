//////////////////////////////////////////////////////////////////////////////
// ContentInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.content.service.requestreply.ContentKind;
import strata.frontendcontroller.service.requestreply.FrontEndInput;
import java.util.ArrayList;
import java.util.List;

public
class ContentInput
    extends FrontEndInput
{
    private String            query;
    private String            articleKey;
    private List<ContentKind> kinds;

    public
    ContentInput()
    {
        super();
        query      = null;
        articleKey = null;
        kinds      = new ArrayList<>();
    }

    @Override
    public ContentInput
    setUserId(Long id)
    {
        return (ContentInput)super.setUserId(id);
    }

    @Override
    public ContentInput
    setSource(String src)
    {
        return (ContentInput)super.setSource(src);
    }

    @Override
    public ContentInput
    setDestination(String dest)
    {
        return (ContentInput)super.setDestination(dest);
    }

    @Override
    public ContentInput
    setCommand(String cmd)
    {
        return (ContentInput)super.setCommand(cmd);
    }

    public ContentInput
    setQuery(String value)
    {
        query = value;
        return this;
    }

    public ContentInput
    setArticleKey(String value)
    {
        articleKey = value;
        return this;
    }

    public ContentInput
    setKinds(List<ContentKind> value)
    {
        kinds = value;
        return this;
    }

    public String
    getQuery() { return query; }

    public String
    getArticleKey() { return articleKey; }

    public List<ContentKind>
    getKinds() { return kinds; }
}

//////////////////////////////////////////////////////////////////////////////
