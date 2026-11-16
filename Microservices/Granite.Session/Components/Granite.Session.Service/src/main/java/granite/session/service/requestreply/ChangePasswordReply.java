//////////////////////////////////////////////////////////////////////////////
// ChangePasswordReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class ChangePasswordReply
    extends AbstractServiceReply
{
    public
    ChangePasswordReply()
    {
        super();
    }

    public
    ChangePasswordReply(ChangePasswordRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public ChangePasswordReply
    setReplyId(UUID replyId)
    {
        return (ChangePasswordReply)super.setReplyId(replyId);
    }

    @Override
    public ChangePasswordReply
    setOriginatingRequestId(UUID requestId)
    {
        return (ChangePasswordReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public ChangePasswordReply
    setTimestamp(Instant timestamp)
    {
        return (ChangePasswordReply)super.setTimestamp(timestamp);
    }

    @Override
    public ChangePasswordReply
    setSuccess(boolean success)
    {
        return (ChangePasswordReply)super.setSuccess(success);
    }

    @Override
    public ChangePasswordReply
    setSuccessMessage(String successMessage)
    {
        return (ChangePasswordReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public ChangePasswordReply
    setFailureMessage(String failureMessage)
    {
        return (ChangePasswordReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public ChangePasswordReply
    setException(ExceptionData exception)
    {
        return (ChangePasswordReply)super.setException(exception);
    }
}

//////////////////////////////////////////////////////////////////////////////
