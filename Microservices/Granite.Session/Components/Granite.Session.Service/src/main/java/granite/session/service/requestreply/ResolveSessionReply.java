//////////////////////////////////////////////////////////////////////////////
// ResolveSessionReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class ResolveSessionReply
    extends AbstractServiceReply
{
    private SessionData session;
    private String      accessToken;

    public
    ResolveSessionReply()
    {
        super();
        session     = null;
        accessToken = null;
    }

    public
    ResolveSessionReply(ResolveSessionRequest originatingRequest)
    {
        super(originatingRequest);
        session     = null;
        accessToken = null;
    }

    @Override
    public ResolveSessionReply
    setReplyId(UUID replyId)
    {
        return (ResolveSessionReply)super.setReplyId(replyId);
    }

    @Override
    public ResolveSessionReply
    setOriginatingRequestId(UUID requestId)
    {
        return (ResolveSessionReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public ResolveSessionReply
    setTimestamp(Instant timestamp)
    {
        return (ResolveSessionReply)super.setTimestamp(timestamp);
    }

    @Override
    public ResolveSessionReply
    setSuccess(boolean success)
    {
        return (ResolveSessionReply)super.setSuccess(success);
    }

    @Override
    public ResolveSessionReply
    setSuccessMessage(String successMessage)
    {
        return (ResolveSessionReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public ResolveSessionReply
    setFailureMessage(String failureMessage)
    {
        return (ResolveSessionReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public ResolveSessionReply
    setException(ExceptionData exception)
    {
        return (ResolveSessionReply)super.setException(exception);
    }

    public ResolveSessionReply
    setSession(SessionData session)
    {
        this.session = session;
        return this;
    }

    public SessionData
    getSession() { return session; }

    public ResolveSessionReply
    setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
        return this;
    }

    public String
    getAccessToken() { return accessToken; }

    @Override
    public String
    toString()
    {
        return
            "ResolveSessionReply{replyId=" + getReplyId()
                + ",success=" + isSuccess()
                + ",sessionId=" + (session != null ? session.getSessionId() : null)
                + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
