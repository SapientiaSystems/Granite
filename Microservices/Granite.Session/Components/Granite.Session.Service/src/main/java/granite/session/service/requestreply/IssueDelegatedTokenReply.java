//////////////////////////////////////////////////////////////////////////////
// IssueDelegatedTokenReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class IssueDelegatedTokenReply
    extends AbstractServiceReply
{
    private String  token;
    private Instant expiresAt;

    public
    IssueDelegatedTokenReply()
    {
        super();
        token     = null;
        expiresAt = null;
    }

    public
    IssueDelegatedTokenReply(IssueDelegatedTokenRequest originatingRequest)
    {
        super(originatingRequest);
        token     = null;
        expiresAt = null;
    }

    @Override
    public IssueDelegatedTokenReply
    setReplyId(UUID replyId)
    {
        return (IssueDelegatedTokenReply)super.setReplyId(replyId);
    }

    @Override
    public IssueDelegatedTokenReply
    setOriginatingRequestId(UUID requestId)
    {
        return (IssueDelegatedTokenReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public IssueDelegatedTokenReply
    setTimestamp(Instant timestamp)
    {
        return (IssueDelegatedTokenReply)super.setTimestamp(timestamp);
    }

    @Override
    public IssueDelegatedTokenReply
    setSuccess(boolean success)
    {
        return (IssueDelegatedTokenReply)super.setSuccess(success);
    }

    @Override
    public IssueDelegatedTokenReply
    setSuccessMessage(String successMessage)
    {
        return (IssueDelegatedTokenReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public IssueDelegatedTokenReply
    setFailureMessage(String failureMessage)
    {
        return (IssueDelegatedTokenReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public IssueDelegatedTokenReply
    setException(ExceptionData exception)
    {
        return (IssueDelegatedTokenReply)super.setException(exception);
    }

    public IssueDelegatedTokenReply
    setToken(String token)
    {
        this.token = token;
        return this;
    }

    public String
    getToken() { return token; }

    public IssueDelegatedTokenReply
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
            "IssueDelegatedTokenReply{replyId=" + getReplyId()
                + ",success=" + isSuccess()
                + ",expiresAt=" + expiresAt
                + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
