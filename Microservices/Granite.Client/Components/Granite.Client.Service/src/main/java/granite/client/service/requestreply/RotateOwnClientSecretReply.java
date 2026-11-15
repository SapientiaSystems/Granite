//////////////////////////////////////////////////////////////////////////////
// RotateOwnClientSecretReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class RotateOwnClientSecretReply
    extends AbstractServiceReply
{
    private String clientId;
    private String clientSecret;

    public
    RotateOwnClientSecretReply()
    {
        super();
        clientId = null;
        clientSecret = null;
    }

    public
    RotateOwnClientSecretReply(RotateOwnClientSecretRequest originatingRequest)
    {
        super(originatingRequest);
        clientId = null;
        clientSecret = null;
    }

    @Override
    public RotateOwnClientSecretReply
    setReplyId(UUID replyId)
    {
        return (RotateOwnClientSecretReply)super.setReplyId(replyId);
    }

    @Override
    public RotateOwnClientSecretReply
    setOriginatingRequestId(UUID requestId)
    {
        return (RotateOwnClientSecretReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public RotateOwnClientSecretReply
    setTimestamp(Instant timestamp)
    {
        return (RotateOwnClientSecretReply)super.setTimestamp(timestamp);
    }

    @Override
    public RotateOwnClientSecretReply
    setSuccess(boolean success)
    {
        return (RotateOwnClientSecretReply)super.setSuccess(success);
    }

    @Override
    public RotateOwnClientSecretReply
    setSuccessMessage(String successMessage)
    {
        return (RotateOwnClientSecretReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public RotateOwnClientSecretReply
    setFailureMessage(String failureMessage)
    {
        return (RotateOwnClientSecretReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public RotateOwnClientSecretReply
    setException(ExceptionData exception)
    {
        return (RotateOwnClientSecretReply)super.setException(exception);
    }

    public RotateOwnClientSecretReply
    setClientId(String value)
    {
        clientId = value;
        return this;
    }

    public RotateOwnClientSecretReply
    setClientSecret(String value)
    {
        clientSecret = value;
        return this;
    }

    public String
    getClientId() { return clientId; }

    public String
    getClientSecret() { return clientSecret; }

    @Override
    public String
    toString()
    {
        return "RotateOwnClientSecretReply{replyId=" + getReplyId() + ",success=" + isSuccess() + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
