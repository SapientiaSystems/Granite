//////////////////////////////////////////////////////////////////////////////
// RotateClientSecretRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class RotateClientSecretRequest
    extends AbstractServiceRequest
{
    private String  clientId;
    private Instant secretExpiresAt;

    public
    RotateClientSecretRequest()
    {
        super();
        clientId = null;
        secretExpiresAt = null;
    }

    @Override
    public RotateClientSecretRequest
    setRequestId(UUID requestId)
    {
        return (RotateClientSecretRequest)super.setRequestId(requestId);
    }

    @Override
    public RotateClientSecretRequest
    setTimestamp(Instant timestamp)
    {
        return (RotateClientSecretRequest)super.setTimestamp(timestamp);
    }

    @Override
    public RotateClientSecretRequest
    addHeader(String name,Object value)
    {
        return (RotateClientSecretRequest)super.addHeader(name,value);
    }

    @Override
    public RotateClientSecretRequest
    clearHeaders()
    {
        return (RotateClientSecretRequest)super.clearHeaders();
    }

    @Override
    public RotateClientSecretRequest
    addCookie(Cookie cookie)
    {
        return (RotateClientSecretRequest)super.addCookie(cookie);
    }

    @Override
    public RotateClientSecretRequest
    clearCookies()
    {
        return (RotateClientSecretRequest)super.clearCookies();
    }

    public RotateClientSecretRequest
    setClientId(String value)
    {
        clientId = value;
        return this;
    }

    public RotateClientSecretRequest
    setSecretExpiresAt(Instant value)
    {
        secretExpiresAt = value;
        return this;
    }

    public String
    getClientId() { return clientId; }

    public Instant
    getSecretExpiresAt() { return secretExpiresAt; }
}

//////////////////////////////////////////////////////////////////////////////
