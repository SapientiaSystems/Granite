//////////////////////////////////////////////////////////////////////////////
// DestroyUsersReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public
class DestroyUsersReply
    extends AbstractServiceReply
{
    private Set<Long>            requestedUserIds;
    private Set<Long>            failedUserIds;
    private List<UserData> destroyedUsers;

    public
    DestroyUsersReply()
    {
        super();
        requestedUserIds = null;
        failedUserIds = null;
        destroyedUsers = null;
    }

    public
    DestroyUsersReply(DestroyUsersRequest originatingRequest)
    {
        super(originatingRequest);
        requestedUserIds = null;
        failedUserIds = null;
        destroyedUsers = null;
    }

    @Override
    public DestroyUsersReply
    setReplyId(UUID replyId)
    {
        return (DestroyUsersReply)super.setReplyId(replyId);
    }

    @Override
    public DestroyUsersReply
    setOriginatingRequestId(UUID requestId)
    {
        return (DestroyUsersReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public DestroyUsersReply
    setTimestamp(Instant timestamp)
    {
        return (DestroyUsersReply)super.setTimestamp(timestamp);
    }

    @Override
    public DestroyUsersReply
    setSuccess(boolean success)
    {
        return (DestroyUsersReply)super.setSuccess(success);
    }

    @Override
    public DestroyUsersReply
    setSuccessMessage(String successMessage)
    {
        return (DestroyUsersReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public DestroyUsersReply
    setFailureMessage(String failureMessage)
    {
        return (DestroyUsersReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public DestroyUsersReply
    setException(ExceptionData exception)
    {
        return (DestroyUsersReply)super.setException(exception);
    }

    public DestroyUsersReply
    setRequestedUserIds(Set<Long> requestedIds)
    {
        requestedUserIds = requestedIds;
        return this;
    }

    public DestroyUsersReply
    setFailedUserIds(Set<Long> failedIds)
    {
        failedUserIds = failedIds;
        return this;
    }

    public DestroyUsersReply
    setDestroyedUsers(List<UserData> destroyed)
    {
        destroyedUsers = destroyed;
        return this;
    }

    public Set<Long>
    getRequestedUserIds() { return requestedUserIds; }

    public Set<Long>
    getFailedUserIds() { return failedUserIds; }

    public List<UserData>
    getDestroyedUsers() { return destroyedUsers; }
}

//////////////////////////////////////////////////////////////////////////////

