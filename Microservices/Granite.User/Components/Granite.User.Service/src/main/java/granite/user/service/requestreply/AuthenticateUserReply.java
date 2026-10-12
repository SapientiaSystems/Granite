//////////////////////////////////////////////////////////////////////////////
// AuthenticateUserReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class AuthenticateUserReply
    extends AbstractServiceReply
{
    private boolean valid;
    private Long    userId;

    public
    AuthenticateUserReply()
    {
        super();
        valid  = false;
        userId = null;
    }

    public
    AuthenticateUserReply(AuthenticateUserRequest originatingRequest)
    {
        super(originatingRequest);
        valid  = false;
        userId = null;
    }

    @Override
    public AuthenticateUserReply
    setReplyId(UUID replyId)
    {
        return (AuthenticateUserReply)super.setReplyId(replyId);
    }

    @Override
    public AuthenticateUserReply
    setOriginatingRequestId(UUID requestId)
    {
        return (AuthenticateUserReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public AuthenticateUserReply
    setTimestamp(Instant timestamp)
    {
        return (AuthenticateUserReply)super.setTimestamp(timestamp);
    }

    @Override
    public AuthenticateUserReply
    setSuccess(boolean success)
    {
        return (AuthenticateUserReply)super.setSuccess(success);
    }

    @Override
    public AuthenticateUserReply
    setSuccessMessage(String successMessage)
    {
        return (AuthenticateUserReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public AuthenticateUserReply
    setFailureMessage(String failureMessage)
    {
        return (AuthenticateUserReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public AuthenticateUserReply
    setException(ExceptionData exception)
    {
        return (AuthenticateUserReply)super.setException(exception);
    }

    public AuthenticateUserReply
    setValid(boolean value)
    {
        valid = value;
        return this;
    }

    public AuthenticateUserReply
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public boolean
    getValid() { return valid; }

    public Long
    getUserId() { return userId; }
}

//////////////////////////////////////////////////////////////////////////////
