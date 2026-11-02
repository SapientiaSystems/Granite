//////////////////////////////////////////////////////////////////////////////
// FindAllContentDocumentsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class FindAllContentDocumentsRequest
    extends AbstractServiceRequest
{
    private Set<ContentKind> kinds;
    private Set<String>      authorities;

    public
    FindAllContentDocumentsRequest()
    {
        super();
        kinds       = new TreeSet<>();
        authorities = new TreeSet<>();
    }

    @Override
    public FindAllContentDocumentsRequest
    setRequestId(UUID requestId)
    {
        return (FindAllContentDocumentsRequest)super.setRequestId(requestId);
    }

    @Override
    public FindAllContentDocumentsRequest
    setTimestamp(Instant timestamp)
    {
        return (FindAllContentDocumentsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindAllContentDocumentsRequest
    addHeader(String name,Object value)
    {
        return (FindAllContentDocumentsRequest)super.addHeader(name,value);
    }

    @Override
    public FindAllContentDocumentsRequest
    clearHeaders()
    {
        return (FindAllContentDocumentsRequest)super.clearHeaders();
    }

    @Override
    public FindAllContentDocumentsRequest
    addCookie(Cookie cookie)
    {
        return (FindAllContentDocumentsRequest)super.addCookie(cookie);
    }

    @Override
    public FindAllContentDocumentsRequest
    clearCookies()
    {
        return (FindAllContentDocumentsRequest)super.clearCookies();
    }

    public FindAllContentDocumentsRequest
    setKinds(Set<ContentKind> value)
    {
        kinds = value;
        return this;
    }

    public FindAllContentDocumentsRequest
    setAuthorities(Set<String> value)
    {
        authorities = value;
        return this;
    }

    public Set<ContentKind>
    getKinds() { return kinds; }

    public Set<String>
    getAuthorities() { return authorities; }
}

//////////////////////////////////////////////////////////////////////////////
