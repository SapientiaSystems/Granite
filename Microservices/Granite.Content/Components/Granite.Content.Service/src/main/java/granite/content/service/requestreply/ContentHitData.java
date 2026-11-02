//////////////////////////////////////////////////////////////////////////////
// ContentHitData.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import java.io.Serializable;

public
class ContentHitData
    implements Serializable
{
    private String      key;
    private String      title;
    private ContentKind kind;
    private String      snippet;
    private Double      score;

    public
    ContentHitData()
    {
        key     = null;
        title   = null;
        kind    = null;
        snippet = null;
        score   = null;
    }

    public ContentHitData
    setKey(String value)
    {
        key = value;
        return this;
    }

    public ContentHitData
    setTitle(String value)
    {
        title = value;
        return this;
    }

    public ContentHitData
    setKind(ContentKind value)
    {
        kind = value;
        return this;
    }

    public ContentHitData
    setSnippet(String value)
    {
        snippet = value;
        return this;
    }

    public ContentHitData
    setScore(Double value)
    {
        score = value;
        return this;
    }

    public String
    getKey() { return key; }

    public String
    getTitle() { return title; }

    public ContentKind
    getKind() { return kind; }

    public String
    getSnippet() { return snippet; }

    public Double
    getScore() { return score; }
}

//////////////////////////////////////////////////////////////////////////////
