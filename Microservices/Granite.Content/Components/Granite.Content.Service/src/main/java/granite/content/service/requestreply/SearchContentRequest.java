//////////////////////////////////////////////////////////////////////////////
// SearchContentRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class SearchContentRequest
    extends AbstractServiceRequest
{
    private String           query;
    private Set<ContentKind> kinds;
    private Set<String>      authorities;
    private Integer          limit;

    public
    SearchContentRequest()
    {
        super();
        query       = null;
        kinds       = new TreeSet<>();
        authorities = new TreeSet<>();
        limit       = null;
    }

    @Override
    public SearchContentRequest
    setRequestId(UUID requestId)
    {
        return (SearchContentRequest)super.setRequestId(requestId);
    }

    @Override
    public SearchContentRequest
    setTimestamp(Instant timestamp)
    {
        return (SearchContentRequest)super.setTimestamp(timestamp);
    }

    @Override
    public SearchContentRequest
    addHeader(String name,Object value)
    {
        return (SearchContentRequest)super.addHeader(name,value);
    }

    @Override
    public SearchContentRequest
    clearHeaders()
    {
        return (SearchContentRequest)super.clearHeaders();
    }

    @Override
    public SearchContentRequest
    addCookie(Cookie cookie)
    {
        return (SearchContentRequest)super.addCookie(cookie);
    }

    @Override
    public SearchContentRequest
    clearCookies()
    {
        return (SearchContentRequest)super.clearCookies();
    }

    public SearchContentRequest
    setQuery(String value)
    {
        query = value;
        return this;
    }

    public SearchContentRequest
    setKinds(Set<ContentKind> value)
    {
        kinds = value;
        return this;
    }

    public SearchContentRequest
    setAuthorities(Set<String> value)
    {
        authorities = value;
        return this;
    }

    public SearchContentRequest
    setLimit(Integer value)
    {
        limit = value;
        return this;
    }

    public String
    getQuery() { return query; }

    public Set<ContentKind>
    getKinds() { return kinds; }

    public Set<String>
    getAuthorities() { return authorities; }

    public Integer
    getLimit() { return limit; }
}

//////////////////////////////////////////////////////////////////////////////
