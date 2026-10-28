//////////////////////////////////////////////////////////////////////////////
// IssueServiceTokenReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class IssueServiceTokenReply
    extends AbstractServiceReply
{
    private String  token;
    private Instant expiresAt;

    public
    IssueServiceTokenReply()
    {
        super();
        token     = null;
        expiresAt = null;
    }

    public
    IssueServiceTokenReply(IssueServiceTokenRequest originatingRequest)
    {
        super(originatingRequest);
        token     = null;
        expiresAt = null;
    }

    @Override
    public IssueServiceTokenReply
    setReplyId(UUID replyId)
    {
        return (IssueServiceTokenReply)super.setReplyId(replyId);
    }

    @Override
    public IssueServiceTokenReply
    setOriginatingRequestId(UUID requestId)
    {
        return (IssueServiceTokenReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public IssueServiceTokenReply
    setTimestamp(Instant timestamp)
    {
        return (IssueServiceTokenReply)super.setTimestamp(timestamp);
    }

    @Override
    public IssueServiceTokenReply
    setSuccess(boolean success)
    {
        return (IssueServiceTokenReply)super.setSuccess(success);
    }

    @Override
    public IssueServiceTokenReply
    setSuccessMessage(String successMessage)
    {
        return (IssueServiceTokenReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public IssueServiceTokenReply
    setFailureMessage(String failureMessage)
    {
        return (IssueServiceTokenReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public IssueServiceTokenReply
    setException(ExceptionData exception)
    {
        return (IssueServiceTokenReply)super.setException(exception);
    }

    public IssueServiceTokenReply
    setToken(String token)
    {
        this.token = token;
        return this;
    }

    public String
    getToken() { return token; }

    public IssueServiceTokenReply
    setExpiresAt(Instant expiresAt)
    {
        this.expiresAt = expiresAt;
        return this;
    }

    public Instant
    getExpiresAt() { return expiresAt; }

    @Override
    public String
    toString()
    {
        return
            "IssueServiceTokenReply{replyId=" + getReplyId()
                + ",success=" + isSuccess()
                + ",expiresAt=" + expiresAt
                + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
