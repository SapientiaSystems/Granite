//////////////////////////////////////////////////////////////////////////////
// StartOktaLoginReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class StartOktaLoginReply
    extends AbstractServiceReply
{
    private String authorizeUrl;
    private String transactionId;

    public
    StartOktaLoginReply()
    {
        super();
        authorizeUrl  = null;
        transactionId = null;
    }

    public
    StartOktaLoginReply(StartOktaLoginRequest originatingRequest)
    {
        super(originatingRequest);
        authorizeUrl  = null;
        transactionId = null;
    }

    @Override
    public StartOktaLoginReply
    setReplyId(UUID replyId)
    {
        return (StartOktaLoginReply)super.setReplyId(replyId);
    }

    @Override
    public StartOktaLoginReply
    setOriginatingRequestId(UUID requestId)
    {
        return (StartOktaLoginReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public StartOktaLoginReply
    setTimestamp(Instant timestamp)
    {
        return (StartOktaLoginReply)super.setTimestamp(timestamp);
    }

    @Override
    public StartOktaLoginReply
    setSuccess(boolean success)
    {
        return (StartOktaLoginReply)super.setSuccess(success);
    }

    @Override
    public StartOktaLoginReply
    setSuccessMessage(String successMessage)
    {
        return (StartOktaLoginReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public StartOktaLoginReply
    setFailureMessage(String failureMessage)
    {
        return (StartOktaLoginReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public StartOktaLoginReply
    setException(ExceptionData exception)
    {
        return (StartOktaLoginReply)super.setException(exception);
    }

    public StartOktaLoginReply
    setAuthorizeUrl(String authorizeUrl)
    {
        this.authorizeUrl = authorizeUrl;
        return this;
    }

    public StartOktaLoginReply
    setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
        return this;
    }

    public String
    getAuthorizeUrl() { return authorizeUrl; }

    public String
    getTransactionId() { return transactionId; }

    @Override
    public String
    toString()
    {
        return "StartOktaLoginReply{replyId=" + getReplyId() + ",success=" + isSuccess() + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
