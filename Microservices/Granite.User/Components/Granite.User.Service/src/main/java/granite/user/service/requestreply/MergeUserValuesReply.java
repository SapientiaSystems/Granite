//////////////////////////////////////////////////////////////////////////////
// MergeUserValuesReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class MergeUserValuesReply
    extends AbstractServiceReply
{
    private UserData mergedUser;

    public
    MergeUserValuesReply()
    {
        super();
        mergedUser = null;
    }

    public
    MergeUserValuesReply(MergeUserValuesRequest originatingRequest)
    {
        super(originatingRequest);
        mergedUser = null;
    }

    @Override
    public MergeUserValuesReply
    setReplyId(UUID replyId)
    {
        return (MergeUserValuesReply)super.setReplyId(replyId);
    }

    @Override
    public MergeUserValuesReply
    setOriginatingRequestId(UUID requestId)
    {
        return (MergeUserValuesReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public MergeUserValuesReply
    setTimestamp(Instant timestamp)
    {
        return (MergeUserValuesReply)super.setTimestamp(timestamp);
    }

    @Override
    public MergeUserValuesReply
    setSuccess(boolean success)
    {
        return (MergeUserValuesReply)super.setSuccess(success);
    }

    @Override
    public MergeUserValuesReply
    setSuccessMessage(String successMessage)
    {
        return (MergeUserValuesReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public MergeUserValuesReply
    setFailureMessage(String failureMessage)
    {
        return (MergeUserValuesReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public MergeUserValuesReply
    setException(ExceptionData exception)
    {
        return (MergeUserValuesReply)super.setException(exception);
    }

    public MergeUserValuesReply
    setMergedUser(UserData merged)
    {
        mergedUser = merged;
        return this;
    }

    public UserData
    getMergedUser() { return mergedUser; }
}

//////////////////////////////////////////////////////////////////////////////
