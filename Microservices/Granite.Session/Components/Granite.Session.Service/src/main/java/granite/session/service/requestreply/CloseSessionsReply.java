//////////////////////////////////////////////////////////////////////////////
// CloseSessionsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class CloseSessionsReply
    extends AbstractServiceReply
{
    private Set<Long>         requestedSessionIds;
    private Set<Long>         failedSessionIds;
    private List<SessionData> closedSessions;

    public
    CloseSessionsReply()
    {
        super();
        requestedSessionIds = new TreeSet<>();
        failedSessionIds    = new TreeSet<>();
        closedSessions      = new ArrayList<>();
    }

    public
    CloseSessionsReply(CloseSessionsRequest originatingRequest)
    {
        super(originatingRequest);
        requestedSessionIds = new TreeSet<>();
        failedSessionIds    = new TreeSet<>();
        closedSessions      = new ArrayList<>();
    }

    @Override
    public CloseSessionsReply
    setReplyId(UUID replyId)
    {
        return (CloseSessionsReply)super.setReplyId(replyId);
    }

    @Override
    public CloseSessionsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (CloseSessionsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public CloseSessionsReply
    setTimestamp(Instant timestamp)
    {
        return (CloseSessionsReply)super.setTimestamp(timestamp);
    }

    @Override
    public CloseSessionsReply
    setSuccess(boolean success)
    {
        return (CloseSessionsReply)super.setSuccess(success);
    }

    @Override
    public CloseSessionsReply
    setSuccessMessage(String successMessage)
    {
        return (CloseSessionsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public CloseSessionsReply
    setFailureMessage(String failureMessage)
    {
        return (CloseSessionsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public CloseSessionsReply
    setException(ExceptionData exception)
    {
        return (CloseSessionsReply)super.setException(exception);
    }

    public CloseSessionsReply
    setRequestedSessionIds(Set<Long> ids)
    {
        requestedSessionIds = ids;
        return this;
    }

    public CloseSessionsReply
    setFailedSessionIds(Set<Long> ids)
    {
        failedSessionIds = ids;
        return this;
    }

    public CloseSessionsReply
    setClosedSessions(List<SessionData> sessions)
    {
        closedSessions = sessions;
        return this;
    }

    public Set<Long>
    getRequestedSessionIds() { return requestedSessionIds; }

    public Set<Long>
    getFailedSessionIds() { return failedSessionIds; }

    public List<SessionData>
    getClosedSessions() { return closedSessions; }
}

//////////////////////////////////////////////////////////////////////////////
