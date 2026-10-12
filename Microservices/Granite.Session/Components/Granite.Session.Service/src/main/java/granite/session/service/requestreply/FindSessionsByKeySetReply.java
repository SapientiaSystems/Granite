//////////////////////////////////////////////////////////////////////////////
// FindSessionsByKeySetReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class FindSessionsByKeySetReply
    extends AbstractServiceReply
{
    private List<SessionData> sessions;

    public
    FindSessionsByKeySetReply()
    {
        super();
        sessions = null;
    }

    public
    FindSessionsByKeySetReply(FindSessionsByKeySetRequest originatingRequest)
    {
        super(originatingRequest);
        sessions = null;
    }

    @Override
    public FindSessionsByKeySetReply
    setReplyId(UUID replyId)
    {
        return (FindSessionsByKeySetReply)super.setReplyId(replyId);
    }

    @Override
    public FindSessionsByKeySetReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindSessionsByKeySetReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindSessionsByKeySetReply
    setTimestamp(Instant timestamp)
    {
        return (FindSessionsByKeySetReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindSessionsByKeySetReply
    setSuccess(boolean success)
    {
        return (FindSessionsByKeySetReply)super.setSuccess(success);
    }

    @Override
    public FindSessionsByKeySetReply
    setSuccessMessage(String successMessage)
    {
        return (FindSessionsByKeySetReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindSessionsByKeySetReply
    setFailureMessage(String failureMessage)
    {
        return (FindSessionsByKeySetReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindSessionsByKeySetReply
    setException(ExceptionData exception)
    {
        return (FindSessionsByKeySetReply)super.setException(exception);
    }

    public FindSessionsByKeySetReply
    setSessions(List<SessionData> value)
    {
        sessions = value;
        return this;
    }

    public List<SessionData>
    getSessions() { return sessions; }
}

//////////////////////////////////////////////////////////////////////////////
