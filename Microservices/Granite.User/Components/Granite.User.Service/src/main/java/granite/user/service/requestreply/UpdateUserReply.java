//////////////////////////////////////////////////////////////////////////////
// UpdateUserReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class UpdateUserReply
    extends AbstractServiceReply
{
    private UserData updatedUser;

    public
    UpdateUserReply()
    {
        super();
        updatedUser = null;
    }

    public
    UpdateUserReply(UpdateUserRequest originatingRequest)
    {
        super(originatingRequest);
        updatedUser = null;
    }

    @Override
    public UpdateUserReply
    setReplyId(UUID replyId)
    {
        return (UpdateUserReply)super.setReplyId(replyId);
    }

    @Override
    public UpdateUserReply
    setOriginatingRequestId(UUID requestId)
    {
        return (UpdateUserReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public UpdateUserReply
    setTimestamp(Instant timestamp)
    {
        return (UpdateUserReply)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateUserReply
    setSuccess(boolean success)
    {
        return (UpdateUserReply)super.setSuccess(success);
    }

    @Override
    public UpdateUserReply
    setSuccessMessage(String successMessage)
    {
        return (UpdateUserReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public UpdateUserReply
    setFailureMessage(String failureMessage)
    {
        return (UpdateUserReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public UpdateUserReply
    setException(ExceptionData exception)
    {
        return (UpdateUserReply)super.setException(exception);
    }

    public UpdateUserReply
    setUpdatedUser(UserData created)
    {
        updatedUser = created;
        return this;
    }

    public UserData
    getUpdatedUser() { return updatedUser; }
}

//////////////////////////////////////////////////////////////////////////////
