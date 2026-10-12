//////////////////////////////////////////////////////////////////////////////
// FindSessionByUserIdAndDeviceIdRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindSessionByUserIdAndDeviceIdRequest
    extends AbstractServiceRequest
{
    private Long userId;
    private String deviceId;

    public
    FindSessionByUserIdAndDeviceIdRequest()
    {
        super();
        userId = null;
        deviceId = null;
    }

    @Override
    public FindSessionByUserIdAndDeviceIdRequest
    setRequestId(UUID requestId)
    {
        return (FindSessionByUserIdAndDeviceIdRequest)super.setRequestId(requestId);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdRequest
    setTimestamp(Instant timestamp)
    {
        return (FindSessionByUserIdAndDeviceIdRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdRequest
    addHeader(String name,Object value)
    {
        return (FindSessionByUserIdAndDeviceIdRequest)super.addHeader(name,value);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdRequest
    clearHeaders()
    {
        return (FindSessionByUserIdAndDeviceIdRequest)super.clearHeaders();
    }

    @Override
    public FindSessionByUserIdAndDeviceIdRequest
    addCookie(Cookie cookie)
    {
        return (FindSessionByUserIdAndDeviceIdRequest)super.addCookie(cookie);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdRequest
    clearCookies()
    {
        return (FindSessionByUserIdAndDeviceIdRequest)super.clearCookies();
    }

    public FindSessionByUserIdAndDeviceIdRequest
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public FindSessionByUserIdAndDeviceIdRequest
    setDeviceId(String value)
    {
        deviceId = value;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public String
    getDeviceId() { return deviceId; }
}

//////////////////////////////////////////////////////////////////////////////
