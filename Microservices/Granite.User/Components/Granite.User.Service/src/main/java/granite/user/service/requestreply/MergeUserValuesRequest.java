//////////////////////////////////////////////////////////////////////////////
// MergeUserValuesRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public
class MergeUserValuesRequest
    extends AbstractServiceRequest
{
    private Long               userId;
    private Map<String,Object> values;

    public
    MergeUserValuesRequest()
    {
        super();
        userId = null;
        values = null;
    }

    @Override
    public MergeUserValuesRequest
    setRequestId(UUID requestId)
    {
        return (MergeUserValuesRequest)super.setRequestId(requestId);
    }

    @Override
    public MergeUserValuesRequest
    setTimestamp(Instant timestamp)
    {
        return (MergeUserValuesRequest)super.setTimestamp(timestamp);
    }

    @Override
    public MergeUserValuesRequest
    addHeader(String name,Object value)
    {
        return (MergeUserValuesRequest)super.addHeader(name,value);
    }

    @Override
    public MergeUserValuesRequest
    clearHeaders()
    {
        return (MergeUserValuesRequest)super.clearHeaders();
    }

    @Override
    public MergeUserValuesRequest
    addCookie(Cookie cookie)
    {
        return (MergeUserValuesRequest)super.addCookie(cookie);
    }

    @Override
    public MergeUserValuesRequest
    clearCookies()
    {
        return (MergeUserValuesRequest)super.clearCookies();
    }

    public MergeUserValuesRequest
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public MergeUserValuesRequest
    setValues(Map<String,Object> entries)
    {
        values = entries;
        return this;
    }

    public Map<String,Object>
    getValues() { return values; }
}

//////////////////////////////////////////////////////////////////////////////
