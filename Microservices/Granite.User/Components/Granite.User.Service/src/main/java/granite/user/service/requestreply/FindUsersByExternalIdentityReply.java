//////////////////////////////////////////////////////////////////////////////
// FindUsersByExternalIdentityReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class FindUsersByExternalIdentityReply
    extends AbstractServiceReply
{
    private List<UserData> users;

    public
    FindUsersByExternalIdentityReply()
    {
        super();
        users = null;
    }

    public
    FindUsersByExternalIdentityReply(FindUsersByExternalIdentityRequest originatingRequest)
    {
        super(originatingRequest);
        users = null;
    }

    @Override
    public FindUsersByExternalIdentityReply
    setReplyId(UUID replyId)
    {
        return (FindUsersByExternalIdentityReply)super.setReplyId(replyId);
    }

    @Override
    public FindUsersByExternalIdentityReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindUsersByExternalIdentityReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindUsersByExternalIdentityReply
    setTimestamp(Instant timestamp)
    {
        return (FindUsersByExternalIdentityReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindUsersByExternalIdentityReply
    setSuccess(boolean success)
    {
        return (FindUsersByExternalIdentityReply)super.setSuccess(success);
    }

    @Override
    public FindUsersByExternalIdentityReply
    setSuccessMessage(String successMessage)
    {
        return (FindUsersByExternalIdentityReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindUsersByExternalIdentityReply
    setFailureMessage(String failureMessage)
    {
        return (FindUsersByExternalIdentityReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindUsersByExternalIdentityReply
    setException(ExceptionData exception)
    {
        return (FindUsersByExternalIdentityReply)super.setException(exception);
    }

    public FindUsersByExternalIdentityReply
    setUsers(List<UserData> value)
    {
        users = value;
        return this;
    }

    public List<UserData>
    getUsers() { return users; }
}

//////////////////////////////////////////////////////////////////////////////
