//////////////////////////////////////////////////////////////////////////////
// UpsertContentDocumentData.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public
class UpsertContentDocumentData
    implements Serializable
{
    private String        key;
    private String        title;
    private ContentKind   kind;
    private ContentFormat format;
    private String        body;
    private List<String>  keywords;
    private Set<String>   requiredAuthorities;
    private String        source;
    private String        version;

    public
    UpsertContentDocumentData()
    {
        key                 = null;
        title               = null;
        kind                = null;
        format              = null;
        body                = null;
        keywords            = new ArrayList<>();
        requiredAuthorities = new TreeSet<>();
        source              = null;
        version             = null;
    }

    public UpsertContentDocumentData
    setKey(String value)
    {
        key = value;
        return this;
    }

    public UpsertContentDocumentData
    setTitle(String value)
    {
        title = value;
        return this;
    }

    public UpsertContentDocumentData
    setKind(ContentKind value)
    {
        kind = value;
        return this;
    }

    public UpsertContentDocumentData
    setFormat(ContentFormat value)
    {
        format = value;
        return this;
    }

    public UpsertContentDocumentData
    setBody(String value)
    {
        body = value;
        return this;
    }

    public UpsertContentDocumentData
    setKeywords(List<String> value)
    {
        keywords = value;
        return this;
    }

    public UpsertContentDocumentData
    setRequiredAuthorities(Set<String> value)
    {
        requiredAuthorities = value;
        return this;
    }

    public UpsertContentDocumentData
    setSource(String value)
    {
        source = value;
        return this;
    }

    public UpsertContentDocumentData
    setVersion(String value)
    {
        version = value;
        return this;
    }

    public String
    getKey() { return key; }

    public String
    getTitle() { return title; }

    public ContentKind
    getKind() { return kind; }

    public ContentFormat
    getFormat() { return format; }

    public String
    getBody() { return body; }

    public List<String>
    getKeywords() { return keywords; }

    public Set<String>
    getRequiredAuthorities() { return requiredAuthorities; }

    public String
    getSource() { return source; }

    public String
    getVersion() { return version; }
}

//////////////////////////////////////////////////////////////////////////////
