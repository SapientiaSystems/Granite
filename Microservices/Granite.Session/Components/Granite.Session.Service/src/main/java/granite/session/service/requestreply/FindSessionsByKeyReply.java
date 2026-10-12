//////////////////////////////////////////////////////////////////////////////
// FindSessionsByKeyReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class FindSessionsByKeyReply
    extends AbstractServiceReply
{
    private List<SessionData> sessions;

    public
    FindSessionsByKeyReply()
    {
        super();
        sessions = null;
    }

    public
    FindSessionsByKeyReply(FindSessionsByKeyRequest originatingRequest)
    {
        super(originatingRequest);
        sessions = null;
    }

    @Override
    public FindSessionsByKeyReply
    setReplyId(UUID replyId)
    {
        return (FindSessionsByKeyReply)super.setReplyId(replyId);
    }

    @Override
    public FindSessionsByKeyReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindSessionsByKeyReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindSessionsByKeyReply
    setTimestamp(Instant timestamp)
    {
        return (FindSessionsByKeyReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindSessionsByKeyReply
    setSuccess(boolean success)
    {
        return (FindSessionsByKeyReply)super.setSuccess(success);
    }

    @Override
    public FindSessionsByKeyReply
    setSuccessMessage(String successMessage)
    {
        return (FindSessionsByKeyReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindSessionsByKeyReply
    setFailureMessage(String failureMessage)
    {
        return (FindSessionsByKeyReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindSessionsByKeyReply
    setException(ExceptionData exception)
    {
        return (FindSessionsByKeyReply)super.setException(exception);
    }

    public FindSessionsByKeyReply
    setSessions(List<SessionData> value)
    {
        sessions = value;
        return this;
    }

    public List<SessionData>
    getSessions() { return sessions; }
}

//////////////////////////////////////////////////////////////////////////////
