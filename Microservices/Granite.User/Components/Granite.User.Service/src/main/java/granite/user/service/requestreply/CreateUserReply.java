//////////////////////////////////////////////////////////////////////////////
// CreateUserReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class CreateUserReply
    extends AbstractServiceReply
{
    private UserData createdUser;

    public
    CreateUserReply()
    {
        super();
        createdUser = null;
    }

    public
    CreateUserReply(CreateUserRequest originatingRequest)
    {
        super(originatingRequest);
        createdUser = null;
    }

    @Override
    public CreateUserReply
    setReplyId(UUID replyId)
    {
        return (CreateUserReply)super.setReplyId(replyId);
    }

    @Override
    public CreateUserReply
    setOriginatingRequestId(UUID requestId)
    {
        return (CreateUserReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public CreateUserReply
    setTimestamp(Instant timestamp)
    {
        return (CreateUserReply)super.setTimestamp(timestamp);
    }

    @Override
    public CreateUserReply
    setSuccess(boolean success)
    {
        return (CreateUserReply)super.setSuccess(success);
    }

    @Override
    public CreateUserReply
    setSuccessMessage(String successMessage)
    {
        return (CreateUserReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public CreateUserReply
    setFailureMessage(String failureMessage)
    {
        return (CreateUserReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public CreateUserReply
    setException(ExceptionData exception)
    {
        return (CreateUserReply)super.setException(exception);
    }

    public CreateUserReply
    setCreatedUser(UserData created)
    {
        createdUser = created;
        return this;
    }

    public UserData
    getCreatedUser() { return createdUser; }
}

//////////////////////////////////////////////////////////////////////////////
