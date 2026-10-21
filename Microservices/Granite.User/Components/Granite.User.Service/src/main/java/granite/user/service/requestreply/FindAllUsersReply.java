//////////////////////////////////////////////////////////////////////////////
// FindAllUsersReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class FindAllUsersReply
    extends AbstractServiceReply
{
    private List<UserData> users;

    public
    FindAllUsersReply()
    {
        super();
        users = null;
    }

    public
    FindAllUsersReply(FindAllUsersRequest originatingRequest)
    {
        super(originatingRequest);
        users = null;
    }

    @Override
    public FindAllUsersReply
    setReplyId(UUID replyId)
    {
        return (FindAllUsersReply)super.setReplyId(replyId);
    }

    @Override
    public FindAllUsersReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindAllUsersReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindAllUsersReply
    setTimestamp(Instant timestamp)
    {
        return (FindAllUsersReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindAllUsersReply
    setSuccess(boolean success)
    {
        return (FindAllUsersReply)super.setSuccess(success);
    }

    @Override
    public FindAllUsersReply
    setSuccessMessage(String successMessage)
    {
        return (FindAllUsersReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindAllUsersReply
    setFailureMessage(String failureMessage)
    {
        return (FindAllUsersReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindAllUsersReply
    setException(ExceptionData exception)
    {
        return (FindAllUsersReply)super.setException(exception);
    }

    public FindAllUsersReply
    setUsers(List<UserData> value)
    {
        users = value;
        return this;
    }

    public List<UserData>
    getUsers() { return users; }
}

//////////////////////////////////////////////////////////////////////////////
