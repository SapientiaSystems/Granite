//////////////////////////////////////////////////////////////////////////////
// ChangeUserPasswordReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class ChangeUserPasswordReply
    extends AbstractServiceReply
{
    public
    ChangeUserPasswordReply()
    {
        super();
    }

    public
    ChangeUserPasswordReply(ChangeUserPasswordRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public ChangeUserPasswordReply
    setReplyId(UUID replyId)
    {
        return (ChangeUserPasswordReply)super.setReplyId(replyId);
    }

    @Override
    public ChangeUserPasswordReply
    setOriginatingRequestId(UUID requestId)
    {
        return (ChangeUserPasswordReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public ChangeUserPasswordReply
    setTimestamp(Instant timestamp)
    {
        return (ChangeUserPasswordReply)super.setTimestamp(timestamp);
    }

    @Override
    public ChangeUserPasswordReply
    setSuccess(boolean success)
    {
        return (ChangeUserPasswordReply)super.setSuccess(success);
    }

    @Override
    public ChangeUserPasswordReply
    setSuccessMessage(String successMessage)
    {
        return (ChangeUserPasswordReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public ChangeUserPasswordReply
    setFailureMessage(String failureMessage)
    {
        return (ChangeUserPasswordReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public ChangeUserPasswordReply
    setException(ExceptionData exception)
    {
        return (ChangeUserPasswordReply)super.setException(exception);
    }
}

//////////////////////////////////////////////////////////////////////////////
