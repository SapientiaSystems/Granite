//////////////////////////////////////////////////////////////////////////////
// FindUsersByKeySetReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class FindUsersByKeySetReply
    extends AbstractServiceReply
{
    private UserKeySet      requestedKeys;
    private UserKeySet      missingKeys;
    private UserKeySet      foundKeys;
    private List<UserData>  foundUsers;
    private boolean               exists;
    private boolean               returnIfFound;

    public
    FindUsersByKeySetReply()
    {
        super();
        requestedKeys = null;
        missingKeys = null;
        foundKeys = null;
        foundUsers = null;
        exists = false;
        returnIfFound = false;
    }

    public
    FindUsersByKeySetReply(FindUsersByKeySetRequest originatingRequest)
    {
        super(originatingRequest);
        requestedKeys = null;
        missingKeys = null;
        foundKeys = null;
        foundUsers = null;
        exists = false;
        returnIfFound = false;
    }

    @Override
    public FindUsersByKeySetReply
    setReplyId(UUID replyId)
    {
        return (FindUsersByKeySetReply)super.setReplyId(replyId);
    }

    @Override
    public FindUsersByKeySetReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindUsersByKeySetReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindUsersByKeySetReply
    setTimestamp(Instant timestamp)
    {
        return (FindUsersByKeySetReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindUsersByKeySetReply
    setSuccess(boolean success)
    {
        return (FindUsersByKeySetReply)super.setSuccess(success);
    }

    @Override
    public FindUsersByKeySetReply
    setSuccessMessage(String successMessage)
    {
        return (FindUsersByKeySetReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindUsersByKeySetReply
    setFailureMessage(String failureMessage)
    {
        return (FindUsersByKeySetReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindUsersByKeySetReply
    setException(ExceptionData exception)
    {
        return (FindUsersByKeySetReply)super.setException(exception);
    }

    public FindUsersByKeySetReply
    setRequestedKeys(UserKeySet value)
    {
        requestedKeys = value;
        return this;
    }

    public FindUsersByKeySetReply
    setMissingKeys(UserKeySet value)
    {
        missingKeys = value;
        return this;
    }

    public FindUsersByKeySetReply
    setFoundKeys(UserKeySet value)
    {
        foundKeys = value;
        return this;
    }

    public FindUsersByKeySetReply
    setFoundUsers(List<UserData> value)
    {
        foundUsers = value;
        return this;
    }

    public FindUsersByKeySetReply
    setExists(boolean value)
    {
        exists = value;
        return this;
    }

    public FindUsersByKeySetReply
    setReturnIfFound(boolean value)
    {
        returnIfFound = value;
        return this;
    }

    public UserKeySet
    getRequestedKeys() { return requestedKeys; }

    public UserKeySet
    getMissingKeys() { return missingKeys; }

    public UserKeySet
    getFoundKeys() { return foundKeys; }

    public List<UserData>
    getFoundUsers() { return foundUsers; }

    public boolean
    getExists() { return exists; }

    public boolean
    getReturnIfFound() { return returnIfFound; }
}

//////////////////////////////////////////////////////////////////////////////
