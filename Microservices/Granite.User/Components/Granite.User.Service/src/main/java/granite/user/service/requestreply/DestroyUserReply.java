//////////////////////////////////////////////////////////////////////////////
// DestroyUserReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class DestroyUserReply
    extends AbstractServiceReply
{
    private UserData destroyedUser;

    public
    DestroyUserReply()
    {
        super();
        destroyedUser = null;
    }

    public
    DestroyUserReply(DestroyUserRequest originatingRequest)
    {
        super(originatingRequest);
        destroyedUser = null;
    }

    @Override
    public DestroyUserReply
    setReplyId(UUID replyId)
    {
        return (DestroyUserReply)super.setReplyId(replyId);
    }

    @Override
    public DestroyUserReply
    setOriginatingRequestId(UUID requestId)
    {
        return (DestroyUserReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public DestroyUserReply
    setTimestamp(Instant timestamp)
    {
        return (DestroyUserReply)super.setTimestamp(timestamp);
    }

    @Override
    public DestroyUserReply
    setSuccess(boolean success)
    {
        return (DestroyUserReply)super.setSuccess(success);
    }

    @Override
    public DestroyUserReply
    setSuccessMessage(String successMessage)
    {
        return (DestroyUserReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public DestroyUserReply
    setFailureMessage(String failureMessage)
    {
        return (DestroyUserReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public DestroyUserReply
    setException(ExceptionData exception)
    {
        return (DestroyUserReply)super.setException(exception);
    }

    public DestroyUserReply
    setDestroyedUser(UserData created)
    {
        destroyedUser = created;
        return this;
    }

    public UserData
    getDestroyedUser() { return destroyedUser; }
}

//////////////////////////////////////////////////////////////////////////////
