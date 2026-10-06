//////////////////////////////////////////////////////////////////////////////
// FindUsersByKeyReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class FindUsersByKeyReply
    extends AbstractServiceReply
{
    private UserKey         key;
    private List<UserData>  foundUsers;
    private boolean               exists;
    private boolean               returnIfFound;

    public
    FindUsersByKeyReply()
    {
        super();
        key = null;
        foundUsers = null;
        exists = false;
        returnIfFound = false;
    }

    public
    FindUsersByKeyReply(FindUsersByKeyRequest originatingRequest)
    {
        super(originatingRequest);
        key = null;
        foundUsers = null;
        exists = false;
        returnIfFound = false;
    }

    @Override
    public FindUsersByKeyReply
    setReplyId(UUID replyId)
    {
        return (FindUsersByKeyReply)super.setReplyId(replyId);
    }

    @Override
    public FindUsersByKeyReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindUsersByKeyReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindUsersByKeyReply
    setTimestamp(Instant timestamp)
    {
        return (FindUsersByKeyReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindUsersByKeyReply
    setSuccess(boolean success)
    {
        return (FindUsersByKeyReply)super.setSuccess(success);
    }

    @Override
    public FindUsersByKeyReply
    setSuccessMessage(String successMessage)
    {
        return (FindUsersByKeyReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindUsersByKeyReply
    setFailureMessage(String failureMessage)
    {
        return (FindUsersByKeyReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindUsersByKeyReply
    setException(ExceptionData exception)
    {
        return (FindUsersByKeyReply)super.setException(exception);
    }

    public FindUsersByKeyReply
    setKey(UserKey value)
    {
        key = value;
        return this;
    }

    public FindUsersByKeyReply
    setFoundUsers(List<UserData> value)
    {
        foundUsers = value;
        return this;
    }

    public FindUsersByKeyReply
    setExists(boolean value)
    {
        exists = value;
        return this;
    }

    public FindUsersByKeyReply
    setReturnIfFound(boolean value)
    {
        returnIfFound = value;
        return this;
    }

    public UserKey
    getKey() { return key; }

    public List<UserData>
    getFoundUsers() { return foundUsers; }

    public boolean
    getExists() { return exists; }

    public boolean
    getReturnIfFound() { return returnIfFound; }
}

//////////////////////////////////////////////////////////////////////////////
