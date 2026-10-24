//////////////////////////////////////////////////////////////////////////////
// LogoutReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class LogoutReply
    extends AbstractServiceReply
{
    private String endSessionUrl;

    public
    LogoutReply()
    {
        super();
        endSessionUrl = null;
    }

    public
    LogoutReply(LogoutRequest originatingRequest)
    {
        super(originatingRequest);
        endSessionUrl = null;
    }

    @Override
    public LogoutReply
    setReplyId(UUID replyId)
    {
        return (LogoutReply)super.setReplyId(replyId);
    }

    @Override
    public LogoutReply
    setOriginatingRequestId(UUID requestId)
    {
        return (LogoutReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public LogoutReply
    setTimestamp(Instant timestamp)
    {
        return (LogoutReply)super.setTimestamp(timestamp);
    }

    @Override
    public LogoutReply
    setSuccess(boolean success)
    {
        return (LogoutReply)super.setSuccess(success);
    }

    @Override
    public LogoutReply
    setSuccessMessage(String successMessage)
    {
        return (LogoutReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public LogoutReply
    setFailureMessage(String failureMessage)
    {
        return (LogoutReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public LogoutReply
    setException(ExceptionData exception)
    {
        return (LogoutReply)super.setException(exception);
    }

    public LogoutReply
    setEndSessionUrl(String endSessionUrl)
    {
        this.endSessionUrl = endSessionUrl;
        return this;
    }

    public String
    getEndSessionUrl() { return endSessionUrl; }

    @Override
    public String
    toString()
    {
        return
            "LogoutReply{replyId=" + getReplyId()
                + ",success=" + isSuccess()
                + ",endSessionUrl=" + (endSessionUrl != null ? "present" : "absent")
                + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
