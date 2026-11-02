//////////////////////////////////////////////////////////////////////////////
// ContentDocumentData.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public
class ContentDocumentData
    implements Serializable
{
    private Long          documentId;
    private String        key;
    private String        title;
    private ContentKind   kind;
    private ContentFormat format;
    private String        body;
    private List<String>  keywords;
    private Set<String>   requiredAuthorities;
    private String        source;
    private String        version;
    private Instant       updated;

    public
    ContentDocumentData()
    {
        documentId          = null;
        key                 = null;
        title               = null;
        kind                = null;
        format              = null;
        body                = null;
        keywords            = new ArrayList<>();
        requiredAuthorities = new TreeSet<>();
        source              = null;
        version             = null;
        updated             = null;
    }

    public ContentDocumentData
    setDocumentId(Long value)
    {
        documentId = value;
        return this;
    }

    public ContentDocumentData
    setKey(String value)
    {
        key = value;
        return this;
    }

    public ContentDocumentData
    setTitle(String value)
    {
        title = value;
        return this;
    }

    public ContentDocumentData
    setKind(ContentKind value)
    {
        kind = value;
        return this;
    }

    public ContentDocumentData
    setFormat(ContentFormat value)
    {
        format = value;
        return this;
    }

    public ContentDocumentData
    setBody(String value)
    {
        body = value;
        return this;
    }

    public ContentDocumentData
    setKeywords(List<String> value)
    {
        keywords = value;
        return this;
    }

    public ContentDocumentData
    setRequiredAuthorities(Set<String> value)
    {
        requiredAuthorities = value;
        return this;
    }

    public ContentDocumentData
    setSource(String value)
    {
        source = value;
        return this;
    }

    public ContentDocumentData
    setVersion(String value)
    {
        version = value;
        return this;
    }

    public ContentDocumentData
    setUpdated(Instant value)
    {
        updated = value;
        return this;
    }

    public Long
    getDocumentId() { return documentId; }

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

    public Instant
    getUpdated() { return updated; }
}

//////////////////////////////////////////////////////////////////////////////
