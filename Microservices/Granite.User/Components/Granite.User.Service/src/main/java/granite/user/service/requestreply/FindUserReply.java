//////////////////////////////////////////////////////////////////////////////
// DestroyUserReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class FindUserReply
    extends AbstractServiceReply
{
    private UserData foundUser;

    public
    FindUserReply()
    {
        super();
        foundUser = null;
    }

    public
    FindUserReply(FindUserRequest originatingRequest)
    {
        super(originatingRequest);
        foundUser = null;
    }

    @Override
    public FindUserReply
    setReplyId(UUID replyId)
    {
        return (FindUserReply)super.setReplyId(replyId);
    }

    @Override
    public FindUserReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindUserReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindUserReply
    setTimestamp(Instant timestamp)
    {
        return (FindUserReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindUserReply
    setSuccess(boolean success)
    {
        return (FindUserReply)super.setSuccess(success);
    }

    @Override
    public FindUserReply
    setSuccessMessage(String successMessage)
    {
        return (FindUserReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindUserReply
    setFailureMessage(String failureMessage)
    {
        return (FindUserReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindUserReply
    setException(ExceptionData exception)
    {
        return (FindUserReply)super.setException(exception);
    }

    public FindUserReply
    setFoundUser(UserData created)
    {
        foundUser = created;
        return this;
    }

    public UserData
    getFoundUser() { return foundUser; }
}

//////////////////////////////////////////////////////////////////////////////
