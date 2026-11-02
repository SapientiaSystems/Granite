//////////////////////////////////////////////////////////////////////////////
// DestroyContentDocumentsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class DestroyContentDocumentsRequest
    extends AbstractServiceRequest
{
    private Set<String> keys;

    public
    DestroyContentDocumentsRequest()
    {
        super();
        keys = new TreeSet<>();
    }

    @Override
    public DestroyContentDocumentsRequest
    setRequestId(UUID requestId)
    {
        return (DestroyContentDocumentsRequest)super.setRequestId(requestId);
    }

    @Override
    public DestroyContentDocumentsRequest
    setTimestamp(Instant timestamp)
    {
        return (DestroyContentDocumentsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public DestroyContentDocumentsRequest
    addHeader(String name,Object value)
    {
        return (DestroyContentDocumentsRequest)super.addHeader(name,value);
    }

    @Override
    public DestroyContentDocumentsRequest
    clearHeaders()
    {
        return (DestroyContentDocumentsRequest)super.clearHeaders();
    }

    @Override
    public DestroyContentDocumentsRequest
    addCookie(Cookie cookie)
    {
        return (DestroyContentDocumentsRequest)super.addCookie(cookie);
    }

    @Override
    public DestroyContentDocumentsRequest
    clearCookies()
    {
        return (DestroyContentDocumentsRequest)super.clearCookies();
    }

    public DestroyContentDocumentsRequest
    setKeys(Set<String> value)
    {
        keys = value;
        return this;
    }

    public Set<String>
    getKeys() { return keys; }
}

//////////////////////////////////////////////////////////////////////////////
