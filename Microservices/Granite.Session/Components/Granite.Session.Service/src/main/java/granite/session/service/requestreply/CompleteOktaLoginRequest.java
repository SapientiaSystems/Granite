//////////////////////////////////////////////////////////////////////////////
// CompleteOktaLoginRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class CompleteOktaLoginRequest
    extends AbstractServiceRequest
{
    private String code;
    private String state;
    private String transactionId;
    private String deviceId;

    public
    CompleteOktaLoginRequest()
    {
        super();
        code          = null;
        state         = null;
        transactionId = null;
        deviceId      = null;
    }

    @Override
    public CompleteOktaLoginRequest
    setRequestId(UUID requestId)
    {
        return (CompleteOktaLoginRequest)super.setRequestId(requestId);
    }

    @Override
    public CompleteOktaLoginRequest
    setTimestamp(Instant timestamp)
    {
        return (CompleteOktaLoginRequest)super.setTimestamp(timestamp);
    }

    @Override
    public CompleteOktaLoginRequest
    addHeader(String name,Object value)
    {
        return (CompleteOktaLoginRequest)super.addHeader(name,value);
    }

    @Override
    public CompleteOktaLoginRequest
    clearHeaders()
    {
        return (CompleteOktaLoginRequest)super.clearHeaders();
    }

    @Override
    public CompleteOktaLoginRequest
    addCookie(Cookie cookie)
    {
        return (CompleteOktaLoginRequest)super.addCookie(cookie);
    }

    @Override
    public CompleteOktaLoginRequest
    clearCookies()
    {
        return (CompleteOktaLoginRequest)super.clearCookies();
    }

    public CompleteOktaLoginRequest
    setCode(String code)
    {
        this.code = code;
        return this;
    }

    public CompleteOktaLoginRequest
    setState(String state)
    {
        this.state = state;
        return this;
    }

    public CompleteOktaLoginRequest
    setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
        return this;
    }

    public CompleteOktaLoginRequest
    setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
        return this;
    }

    public String
    getCode() { return code; }

    public String
    getState() { return state; }

    public String
    getTransactionId() { return transactionId; }

    public String
    getDeviceId() { return deviceId; }

    @Override
    public String
    toString()
    {
        return "CompleteOktaLoginRequest{requestId=" + getRequestId() + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
