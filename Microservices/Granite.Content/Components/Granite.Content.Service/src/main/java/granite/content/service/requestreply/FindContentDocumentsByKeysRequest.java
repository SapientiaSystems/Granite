//////////////////////////////////////////////////////////////////////////////
// FindContentDocumentsByKeysRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class FindContentDocumentsByKeysRequest
    extends AbstractServiceRequest
{
    private Set<String> keys;
    private Set<String> authorities;

    public
    FindContentDocumentsByKeysRequest()
    {
        super();
        keys        = new TreeSet<>();
        authorities = new TreeSet<>();
    }

    @Override
    public FindContentDocumentsByKeysRequest
    setRequestId(UUID requestId)
    {
        return (FindContentDocumentsByKeysRequest)super.setRequestId(requestId);
    }

    @Override
    public FindContentDocumentsByKeysRequest
    setTimestamp(Instant timestamp)
    {
        return (FindContentDocumentsByKeysRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindContentDocumentsByKeysRequest
    addHeader(String name,Object value)
    {
        return (FindContentDocumentsByKeysRequest)super.addHeader(name,value);
    }

    @Override
    public FindContentDocumentsByKeysRequest
    clearHeaders()
    {
        return (FindContentDocumentsByKeysRequest)super.clearHeaders();
    }

    @Override
    public FindContentDocumentsByKeysRequest
    addCookie(Cookie cookie)
    {
        return (FindContentDocumentsByKeysRequest)super.addCookie(cookie);
    }

    @Override
    public FindContentDocumentsByKeysRequest
    clearCookies()
    {
        return (FindContentDocumentsByKeysRequest)super.clearCookies();
    }

    public FindContentDocumentsByKeysRequest
    setKeys(Set<String> value)
    {
        keys = value;
        return this;
    }

    public FindContentDocumentsByKeysRequest
    setAuthorities(Set<String> value)
    {
        authorities = value;
        return this;
    }

    public Set<String>
    getKeys() { return keys; }

    public Set<String>
    getAuthorities() { return authorities; }
}

//////////////////////////////////////////////////////////////////////////////
