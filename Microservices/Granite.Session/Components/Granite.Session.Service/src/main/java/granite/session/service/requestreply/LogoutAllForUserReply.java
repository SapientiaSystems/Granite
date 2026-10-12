//////////////////////////////////////////////////////////////////////////////
// LogoutAllForUserReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class LogoutAllForUserReply
    extends AbstractServiceReply
{
    private Set<Long> destroyed;

    public
    LogoutAllForUserReply()
    {
        super();
        destroyed = new TreeSet<>();
    }

    public
    LogoutAllForUserReply(LogoutAllForUserRequest originatingRequest)
    {
        super(originatingRequest);
        destroyed = new TreeSet<>();
    }

    @Override
    public LogoutAllForUserReply
    setReplyId(UUID replyId)
    {
        return (LogoutAllForUserReply)super.setReplyId(replyId);
    }

    @Override
    public LogoutAllForUserReply
    setOriginatingRequestId(UUID requestId)
    {
        return (LogoutAllForUserReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public LogoutAllForUserReply
    setTimestamp(Instant timestamp)
    {
        return (LogoutAllForUserReply)super.setTimestamp(timestamp);
    }

    @Override
    public LogoutAllForUserReply
    setSuccess(boolean success)
    {
        return (LogoutAllForUserReply)super.setSuccess(success);
    }

    @Override
    public LogoutAllForUserReply
    setSuccessMessage(String successMessage)
    {
        return (LogoutAllForUserReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public LogoutAllForUserReply
    setFailureMessage(String failureMessage)
    {
        return (LogoutAllForUserReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public LogoutAllForUserReply
    setException(ExceptionData exception)
    {
        return (LogoutAllForUserReply)super.setException(exception);
    }

    public LogoutAllForUserReply
    setDestroyed(Set<Long> destroyed)
    {
        this.destroyed = destroyed;
        return this;
    }

    public Set<Long>
    getDestroyed() { return destroyed; }
}

//////////////////////////////////////////////////////////////////////////////
