//////////////////////////////////////////////////////////////////////////////
// CreateUsersReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class CreateUsersReply
    extends AbstractServiceReply
{
    private List<CreateUserData> requestedUsers;
    private List<CreateUserData> failedUsers;
    private List<UserData>       createdUsers;

    public
    CreateUsersReply()
    {
        super();
        requestedUsers = null;
        failedUsers = null;
        createdUsers = null;
    }

    public
    CreateUsersReply(CreateUsersRequest originatingRequest)
    {
        super(originatingRequest);
        requestedUsers = null;
        failedUsers = null;
        createdUsers = null;
    }

    @Override
    public CreateUsersReply
    setReplyId(UUID replyId)
    {
        return (CreateUsersReply)super.setReplyId(replyId);
    }

    @Override
    public CreateUsersReply
    setOriginatingRequestId(UUID requestId)
    {
        return (CreateUsersReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public CreateUsersReply
    setTimestamp(Instant timestamp)
    {
        return (CreateUsersReply)super.setTimestamp(timestamp);
    }

    @Override
    public CreateUsersReply
    setSuccess(boolean success)
    {
        return (CreateUsersReply)super.setSuccess(success);
    }

    @Override
    public CreateUsersReply
    setSuccessMessage(String successMessage)
    {
        return (CreateUsersReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public CreateUsersReply
    setFailureMessage(String failureMessage)
    {
        return (CreateUsersReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public CreateUsersReply
    setException(ExceptionData exception)
    {
        return (CreateUsersReply)super.setException(exception);
    }

    public CreateUsersReply
    setRequestedUsers(List<CreateUserData> requested)
    {
        requestedUsers = requested;
        return this;
    }

    public CreateUsersReply
    setFailedUsers(List<CreateUserData> failed)
    {
        failedUsers = failed;
        return this;
    }

    public CreateUsersReply
    setCreatedUsers(List<UserData> created)
    {
        createdUsers = created;
        return this;
    }

    public List<CreateUserData>
    getRequestedUsers() { return requestedUsers; }

    public List<CreateUserData>
    getFailedUsers() { return failedUsers; }

    public List<UserData>
    getCreatedUsers() { return createdUsers; }
}

//////////////////////////////////////////////////////////////////////////////
