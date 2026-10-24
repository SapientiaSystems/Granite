//////////////////////////////////////////////////////////////////////////////
// StartOktaLoginRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class StartOktaLoginRequest
    extends AbstractServiceRequest
{
    private String deviceId;
    private String returnTo;

    public
    StartOktaLoginRequest()
    {
        super();
        deviceId = null;
        returnTo = null;
    }

    @Override
    public StartOktaLoginRequest
    setRequestId(UUID requestId)
    {
        return (StartOktaLoginRequest)super.setRequestId(requestId);
    }

    @Override
    public StartOktaLoginRequest
    setTimestamp(Instant timestamp)
    {
        return (StartOktaLoginRequest)super.setTimestamp(timestamp);
    }

    @Override
    public StartOktaLoginRequest
    addHeader(String name,Object value)
    {
        return (StartOktaLoginRequest)super.addHeader(name,value);
    }

    @Override
    public StartOktaLoginRequest
    clearHeaders()
    {
        return (StartOktaLoginRequest)super.clearHeaders();
    }

    @Override
    public StartOktaLoginRequest
    addCookie(Cookie cookie)
    {
        return (StartOktaLoginRequest)super.addCookie(cookie);
    }

    @Override
    public StartOktaLoginRequest
    clearCookies()
    {
        return (StartOktaLoginRequest)super.clearCookies();
    }

    public StartOktaLoginRequest
    setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
        return this;
    }

    public StartOktaLoginRequest
    setReturnTo(String returnTo)
    {
        this.returnTo = returnTo;
        return this;
    }

    public String
    getDeviceId() { return deviceId; }

    public String
    getReturnTo() { return returnTo; }

    @Override
    public String
    toString()
    {
        return "StartOktaLoginRequest{requestId=" + getRequestId() + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
