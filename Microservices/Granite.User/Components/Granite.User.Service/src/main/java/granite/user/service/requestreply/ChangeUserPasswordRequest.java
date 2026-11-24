//////////////////////////////////////////////////////////////////////////////
// ChangeUserPasswordRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class ChangeUserPasswordRequest
    extends AbstractServiceRequest
{
    private Long   userId;
    private String currentPassword;
    private String newPassword;

    public
    ChangeUserPasswordRequest()
    {
        super();
        userId          = null;
        currentPassword = null;
        newPassword     = null;
    }

    @Override
    public ChangeUserPasswordRequest
    setRequestId(UUID requestId)
    {
        return (ChangeUserPasswordRequest)super.setRequestId(requestId);
    }

    @Override
    public ChangeUserPasswordRequest
    setTimestamp(Instant timestamp)
    {
        return (ChangeUserPasswordRequest)super.setTimestamp(timestamp);
    }

    @Override
    public ChangeUserPasswordRequest
    addHeader(String name,Object value)
    {
        return (ChangeUserPasswordRequest)super.addHeader(name,value);
    }

    @Override
    public ChangeUserPasswordRequest
    clearHeaders()
    {
        return (ChangeUserPasswordRequest)super.clearHeaders();
    }

    @Override
    public ChangeUserPasswordRequest
    addCookie(Cookie cookie)
    {
        return (ChangeUserPasswordRequest)super.addCookie(cookie);
    }

    @Override
    public ChangeUserPasswordRequest
    clearCookies()
    {
        return (ChangeUserPasswordRequest)super.clearCookies();
    }

    public ChangeUserPasswordRequest
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public ChangeUserPasswordRequest
    setCurrentPassword(String value)
    {
        currentPassword = value;
        return this;
    }

    public ChangeUserPasswordRequest
    setNewPassword(String value)
    {
        newPassword = value;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public String
    getCurrentPassword() { return currentPassword; }

    public String
    getNewPassword() { return newPassword; }

    @Override
    public String
    toString()
    {
        return "ChangeUserPasswordRequest{requestId=" + getRequestId() + ", userId=" + userId + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
