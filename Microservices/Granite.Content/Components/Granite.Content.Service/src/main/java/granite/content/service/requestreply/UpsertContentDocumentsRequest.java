//////////////////////////////////////////////////////////////////////////////
// UpsertContentDocumentsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class UpsertContentDocumentsRequest
    extends AbstractServiceRequest
{
    private List<UpsertContentDocumentData> documents;

    public
    UpsertContentDocumentsRequest()
    {
        super();
        documents = new ArrayList<>();
    }

    @Override
    public UpsertContentDocumentsRequest
    setRequestId(UUID requestId)
    {
        return (UpsertContentDocumentsRequest)super.setRequestId(requestId);
    }

    @Override
    public UpsertContentDocumentsRequest
    setTimestamp(Instant timestamp)
    {
        return (UpsertContentDocumentsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public UpsertContentDocumentsRequest
    addHeader(String name,Object value)
    {
        return (UpsertContentDocumentsRequest)super.addHeader(name,value);
    }

    @Override
    public UpsertContentDocumentsRequest
    clearHeaders()
    {
        return (UpsertContentDocumentsRequest)super.clearHeaders();
    }

    @Override
    public UpsertContentDocumentsRequest
    addCookie(Cookie cookie)
    {
        return (UpsertContentDocumentsRequest)super.addCookie(cookie);
    }

    @Override
    public UpsertContentDocumentsRequest
    clearCookies()
    {
        return (UpsertContentDocumentsRequest)super.clearCookies();
    }

    public UpsertContentDocumentsRequest
    setDocuments(List<UpsertContentDocumentData> value)
    {
        documents = value;
        return this;
    }

    public List<UpsertContentDocumentData>
    getDocuments() { return documents; }
}

//////////////////////////////////////////////////////////////////////////////
