//////////////////////////////////////////////////////////////////////////////
// RotateOwnedClientSecretRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class RotateOwnedClientSecretRequest
    extends AbstractServiceRequest
{
    private String clientId;
    private Long   ownerUserId;

    public
    RotateOwnedClientSecretRequest()
    {
        super();
        clientId = null;
        ownerUserId = null;
    }

    @Override
    public RotateOwnedClientSecretRequest
    setRequestId(UUID requestId)
    {
        return (RotateOwnedClientSecretRequest)super.setRequestId(requestId);
    }

    @Override
    public RotateOwnedClientSecretRequest
    setTimestamp(Instant timestamp)
    {
        return (RotateOwnedClientSecretRequest)super.setTimestamp(timestamp);
    }

    @Override
    public RotateOwnedClientSecretRequest
    addHeader(String name,Object value)
    {
        return (RotateOwnedClientSecretRequest)super.addHeader(name,value);
    }

    @Override
    public RotateOwnedClientSecretRequest
    clearHeaders()
    {
        return (RotateOwnedClientSecretRequest)super.clearHeaders();
    }

    @Override
    public RotateOwnedClientSecretRequest
    addCookie(Cookie cookie)
    {
        return (RotateOwnedClientSecretRequest)super.addCookie(cookie);
    }

    @Override
    public RotateOwnedClientSecretRequest
    clearCookies()
    {
        return (RotateOwnedClientSecretRequest)super.clearCookies();
    }

    public RotateOwnedClientSecretRequest
    setClientId(String value)
    {
        clientId = value;
        return this;
    }

    public RotateOwnedClientSecretRequest
    setOwnerUserId(Long value)
    {
        ownerUserId = value;
        return this;
    }

    public String
    getClientId() { return clientId; }

    public Long
    getOwnerUserId() { return ownerUserId; }
}

//////////////////////////////////////////////////////////////////////////////
