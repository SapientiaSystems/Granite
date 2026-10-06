//////////////////////////////////////////////////////////////////////////////
// UpdateUsersReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class UpdateUsersReply
    extends AbstractServiceReply
{
    private List<UpdateUserData> requestedUsers;
    private List<UpdateUserData> failedUsers;
    private List<UserData>       updatedUsers;

    public
    UpdateUsersReply()
    {
        super();
        requestedUsers = null;
        failedUsers = null;
        updatedUsers = null;
    }

    public
    UpdateUsersReply(UpdateUsersRequest originatingRequest)
    {
        super(originatingRequest);
        requestedUsers = null;
        failedUsers = null;
        updatedUsers = null;
    }

    @Override
    public UpdateUsersReply
    setReplyId(UUID replyId)
    {
        return (UpdateUsersReply)super.setReplyId(replyId);
    }

    @Override
    public UpdateUsersReply
    setOriginatingRequestId(UUID requestId)
    {
        return (UpdateUsersReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public UpdateUsersReply
    setTimestamp(Instant timestamp)
    {
        return (UpdateUsersReply)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateUsersReply
    setSuccess(boolean success)
    {
        return (UpdateUsersReply)super.setSuccess(success);
    }

    @Override
    public UpdateUsersReply
    setSuccessMessage(String successMessage)
    {
        return (UpdateUsersReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public UpdateUsersReply
    setFailureMessage(String failureMessage)
    {
        return (UpdateUsersReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public UpdateUsersReply
    setException(ExceptionData exception)
    {
        return (UpdateUsersReply)super.setException(exception);
    }

    public UpdateUsersReply
    setRequestedUsers(List<UpdateUserData> requested)
    {
        requestedUsers = requested;
        return this;
    }

    public UpdateUsersReply
    setFailedUsers(List<UpdateUserData> failed)
    {
        failedUsers = failed;
        return this;
    }

    public UpdateUsersReply
    setUpdatedUsers(List<UserData> updated)
    {
        updatedUsers = updated;
        return this;
    }

    public List<UpdateUserData>
    getRequestedUsers() { return requestedUsers; }

    public List<UpdateUserData>
    getFailedUsers() { return failedUsers; }

    public List<UserData>
    getUpdatedUsers() { return updatedUsers; }
}

//////////////////////////////////////////////////////////////////////////////
