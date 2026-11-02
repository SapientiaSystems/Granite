//////////////////////////////////////////////////////////////////////////////
// FindSessionsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class FindSessionsReply
    extends AbstractServiceReply
{
    private List<SessionData> sessions;

    public
    FindSessionsReply()
    {
        super();
        sessions = null;
    }

    public
    FindSessionsReply(FindSessionsRequest originatingRequest)
    {
        super(originatingRequest);
        sessions = null;
    }

    @Override
    public FindSessionsReply
    setReplyId(UUID replyId)
    {
        return (FindSessionsReply)super.setReplyId(replyId);
    }

    @Override
    public FindSessionsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindSessionsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindSessionsReply
    setTimestamp(Instant timestamp)
    {
        return (FindSessionsReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindSessionsReply
    setSuccess(boolean success)
    {
        return (FindSessionsReply)super.setSuccess(success);
    }

    @Override
    public FindSessionsReply
    setSuccessMessage(String successMessage)
    {
        return (FindSessionsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindSessionsReply
    setFailureMessage(String failureMessage)
    {
        return (FindSessionsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindSessionsReply
    setException(ExceptionData exception)
    {
        return (FindSessionsReply)super.setException(exception);
    }

    public FindSessionsReply
    setSessions(List<SessionData> value)
    {
        sessions = value;
        return this;
    }

    public List<SessionData>
    getSessions() { return sessions; }
}

//////////////////////////////////////////////////////////////////////////////
