//////////////////////////////////////////////////////////////////////////////
// ContentDocument.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

import granite.content.service.requestreply.ContentFormat;
import granite.content.service.requestreply.ContentKind;
import strata.server.core.entity.AbstractEntityAndDomainEventSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public
class ContentDocument
    extends
        AbstractEntityAndDomainEventSource<
            Long,
            ContentDocument,
            IContentDocumentEvent,
            IContentDocumentEventObserver>
    implements IContentDocumentEventSource
{
    private String        documentKey;
    private String        title;
    private ContentKind   kind;
    private ContentFormat format;
    private String        body;
    private List<String>  keywords;
    private Set<String>   requiredAuthorities;
    private String        source;
    private String        contentVersion;
    private Instant       changed;

    public
    ContentDocument()
    {
        super();
        documentKey         = null;
        title               = null;
        kind                = ContentKind.HELP;
        format              = ContentFormat.MARKDOWN;
        body                = null;
        keywords            = new ArrayList<>();
        requiredAuthorities = new TreeSet<>();
        source              = null;
        contentVersion      = null;
        changed             = null;
    }

    @Override
    public ContentDocument
    setPrimaryId(Long primaryId)
    {
        return super.setPrimaryId(primaryId);
    }

    @Override
    public Long
    getPrimaryId()
    {
        return super.getPrimaryId();
    }

    public ContentDocument
    setDocumentKey(String value)
    {
        documentKey = value;
        return this;
    }

    public ContentDocument
    setTitle(String value)
    {
        title = value;
        return this;
    }

    public ContentDocument
    setKind(ContentKind value)
    {
        kind = value;
        return this;
    }

    public ContentDocument
    setFormat(ContentFormat value)
    {
        format = value;
        return this;
    }

    public ContentDocument
    setBody(String value)
    {
        body = value;
        return this;
    }

    public ContentDocument
    setKeywords(List<String> value)
    {
        keywords = value == null ? new ArrayList<>() : new ArrayList<>(value);
        return this;
    }

    public ContentDocument
    setRequiredAuthorities(Set<String> value)
    {
        requiredAuthorities = value == null ? new TreeSet<>() : new TreeSet<>(value);
        return this;
    }

    public ContentDocument
    setSource(String value)
    {
        source = value;
        return this;
    }

    public ContentDocument
    setContentVersion(String value)
    {
        contentVersion = value;
        return this;
    }

    public ContentDocument
    setChanged(Instant time)
    {
        changed = time;
        return this;
    }

    public boolean
    isVisibleTo(Set<String> authorities)
    {
        if (requiredAuthorities.isEmpty())
            return true;

        if (authorities == null)
            return false;

        for (String authority : requiredAuthorities)
            if (authorities.contains(authority))
                return true;

        return false;
    }

    public Long
    getDocumentId() { return super.getPrimaryId(); }

    public String
    getDocumentKey() { return documentKey; }

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
    getContentVersion() { return contentVersion; }

    public Instant
    getChanged() { return changed; }

    public ContentDocument
    notifyCreated() { return super.notify(new ContentDocumentCreated(this)); }

    public ContentDocument
    notifyUpdated() { return super.notify(new ContentDocumentUpdated(this)); }

    public ContentDocument
    notifyDestroyed() { return super.notify(new ContentDocumentDestroyed(this)); }

    @Override
    protected ContentDocument
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
