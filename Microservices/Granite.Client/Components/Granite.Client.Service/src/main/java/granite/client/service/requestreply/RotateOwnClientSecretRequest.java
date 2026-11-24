//////////////////////////////////////////////////////////////////////////////
// RotateOwnClientSecretRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class RotateOwnClientSecretRequest
    extends AbstractServiceRequest
{
    public
    RotateOwnClientSecretRequest()
    {
        super();
    }

    @Override
    public RotateOwnClientSecretRequest
    setRequestId(UUID requestId)
    {
        return (RotateOwnClientSecretRequest)super.setRequestId(requestId);
    }

    @Override
    public RotateOwnClientSecretRequest
    setTimestamp(Instant timestamp)
    {
        return (RotateOwnClientSecretRequest)super.setTimestamp(timestamp);
    }

    @Override
    public RotateOwnClientSecretRequest
    addHeader(String name,Object value)
    {
        return (RotateOwnClientSecretRequest)super.addHeader(name,value);
    }

    @Override
    public RotateOwnClientSecretRequest
    clearHeaders()
    {
        return (RotateOwnClientSecretRequest)super.clearHeaders();
    }

    @Override
    public RotateOwnClientSecretRequest
    addCookie(Cookie cookie)
    {
        return (RotateOwnClientSecretRequest)super.addCookie(cookie);
    }

    @Override
    public RotateOwnClientSecretRequest
    clearCookies()
    {
        return (RotateOwnClientSecretRequest)super.clearCookies();
    }
}

//////////////////////////////////////////////////////////////////////////////
