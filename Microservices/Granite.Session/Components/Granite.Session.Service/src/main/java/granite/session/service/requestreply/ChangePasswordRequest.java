//////////////////////////////////////////////////////////////////////////////
// ChangePasswordRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class ChangePasswordRequest
    extends AbstractServiceRequest
{
    private Long   sessionId;
    private String currentPassword;
    private String newPassword;

    public
    ChangePasswordRequest()
    {
        super();
        sessionId       = null;
        currentPassword = null;
        newPassword     = null;
    }

    @Override
    public ChangePasswordRequest
    setRequestId(UUID requestId)
    {
        return (ChangePasswordRequest)super.setRequestId(requestId);
    }

    @Override
    public ChangePasswordRequest
    setTimestamp(Instant timestamp)
    {
        return (ChangePasswordRequest)super.setTimestamp(timestamp);
    }

    @Override
    public ChangePasswordRequest
    addHeader(String name,Object value)
    {
        return (ChangePasswordRequest)super.addHeader(name,value);
    }

    @Override
    public ChangePasswordRequest
    clearHeaders()
    {
        return (ChangePasswordRequest)super.clearHeaders();
    }

    @Override
    public ChangePasswordRequest
    addCookie(Cookie cookie)
    {
        return (ChangePasswordRequest)super.addCookie(cookie);
    }

    @Override
    public ChangePasswordRequest
    clearCookies()
    {
        return (ChangePasswordRequest)super.clearCookies();
    }

    public ChangePasswordRequest
    setSessionId(Long value)
    {
        sessionId = value;
        return this;
    }

    public ChangePasswordRequest
    setCurrentPassword(String value)
    {
        currentPassword = value;
        return this;
    }

    public ChangePasswordRequest
    setNewPassword(String value)
    {
        newPassword = value;
        return this;
    }

    public Long
    getSessionId() { return sessionId; }

    public String
    getCurrentPassword() { return currentPassword; }

    public String
    getNewPassword() { return newPassword; }

    @Override
    public String
    toString()
    {
        return "ChangePasswordRequest{requestId=" + getRequestId() + ", sessionId=" + sessionId + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
