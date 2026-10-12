//////////////////////////////////////////////////////////////////////////////
// RefreshReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class RefreshReply
    extends AbstractServiceReply
{
    private SessionData sessionData;
    private String accessToken;

    public
    RefreshReply()
    {
        super();
        sessionData = null;
        accessToken = null;
    }

    public
    RefreshReply(RefreshRequest originatingRequest)
    {
        super(originatingRequest);
        sessionData = null;
        accessToken = null;
    }

    @Override
    public RefreshReply
    setReplyId(UUID replyId)
    {
        return (RefreshReply)super.setReplyId(replyId);
    }

    @Override
    public RefreshReply
    setOriginatingRequestId(UUID requestId)
    {
        return (RefreshReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public RefreshReply
    setTimestamp(Instant timestamp)
    {
        return (RefreshReply)super.setTimestamp(timestamp);
    }

    @Override
    public RefreshReply
    setSuccess(boolean success)
    {
        return (RefreshReply)super.setSuccess(success);
    }

    @Override
    public RefreshReply
    setSuccessMessage(String successMessage)
    {
        return (RefreshReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public RefreshReply
    setFailureMessage(String failureMessage)
    {
        return (RefreshReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public RefreshReply
    setException(ExceptionData exception)
    {
        return (RefreshReply)super.setException(exception);
    }

    public RefreshReply
    setSessionData(SessionData sessionData)
    {
        this.sessionData = sessionData;
        return this;
    }

    public SessionData
    getSessionData() { return sessionData; }

    public RefreshReply
    setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
        return this;
    }

    public String
    getAccessToken() { return accessToken; }
}

//////////////////////////////////////////////////////////////////////////////
