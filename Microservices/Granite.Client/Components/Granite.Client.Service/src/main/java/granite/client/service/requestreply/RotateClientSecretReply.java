//////////////////////////////////////////////////////////////////////////////
// RotateClientSecretReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class RotateClientSecretReply
    extends AbstractServiceReply
{
    private ClientData client;
    private String     clientSecret;

    public
    RotateClientSecretReply()
    {
        super();
        client = null;
        clientSecret = null;
    }

    public
    RotateClientSecretReply(RotateClientSecretRequest originatingRequest)
    {
        super(originatingRequest);
        client = null;
        clientSecret = null;
    }

    @Override
    public RotateClientSecretReply
    setReplyId(UUID replyId)
    {
        return (RotateClientSecretReply)super.setReplyId(replyId);
    }

    @Override
    public RotateClientSecretReply
    setOriginatingRequestId(UUID requestId)
    {
        return (RotateClientSecretReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public RotateClientSecretReply
    setTimestamp(Instant timestamp)
    {
        return (RotateClientSecretReply)super.setTimestamp(timestamp);
    }

    @Override
    public RotateClientSecretReply
    setSuccess(boolean success)
    {
        return (RotateClientSecretReply)super.setSuccess(success);
    }

    @Override
    public RotateClientSecretReply
    setSuccessMessage(String successMessage)
    {
        return (RotateClientSecretReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public RotateClientSecretReply
    setFailureMessage(String failureMessage)
    {
        return (RotateClientSecretReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public RotateClientSecretReply
    setException(ExceptionData exception)
    {
        return (RotateClientSecretReply)super.setException(exception);
    }

    public RotateClientSecretReply
    setClient(ClientData value)
    {
        client = value;
        return this;
    }

    public RotateClientSecretReply
    setClientSecret(String value)
    {
        clientSecret = value;
        return this;
    }

    public ClientData
    getClient() { return client; }

    public String
    getClientSecret() { return clientSecret; }

    @Override
    public String
    toString()
    {
        return "RotateClientSecretReply{replyId=" + getReplyId() + ",success=" + isSuccess() + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
