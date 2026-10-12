//////////////////////////////////////////////////////////////////////////////
// FindUsersByExternalIdentityRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindUsersByExternalIdentityRequest
    extends AbstractServiceRequest
{
    private String provider;
    private String externalId;

    public
    FindUsersByExternalIdentityRequest()
    {
        super();
        provider   = null;
        externalId = null;
    }

    @Override
    public FindUsersByExternalIdentityRequest
    setRequestId(UUID requestId)
    {
        return (FindUsersByExternalIdentityRequest)super.setRequestId(requestId);
    }

    @Override
    public FindUsersByExternalIdentityRequest
    setTimestamp(Instant timestamp)
    {
        return (FindUsersByExternalIdentityRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindUsersByExternalIdentityRequest
    addHeader(String name,Object value)
    {
        return (FindUsersByExternalIdentityRequest)super.addHeader(name,value);
    }

    @Override
    public FindUsersByExternalIdentityRequest
    clearHeaders()
    {
        return (FindUsersByExternalIdentityRequest)super.clearHeaders();
    }

    @Override
    public FindUsersByExternalIdentityRequest
    addCookie(Cookie cookie)
    {
        return (FindUsersByExternalIdentityRequest)super.addCookie(cookie);
    }

    @Override
    public FindUsersByExternalIdentityRequest
    clearCookies()
    {
        return (FindUsersByExternalIdentityRequest)super.clearCookies();
    }

    public FindUsersByExternalIdentityRequest
    setProvider(String value)
    {
        provider = value;
        return this;
    }

    public FindUsersByExternalIdentityRequest
    setExternalId(String value)
    {
        externalId = value;
        return this;
    }

    public String
    getProvider() { return provider; }

    public String
    getExternalId() { return externalId; }
}

//////////////////////////////////////////////////////////////////////////////
