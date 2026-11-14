//////////////////////////////////////////////////////////////////////////////
// CreateClientReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class CreateClientReply
    extends AbstractServiceReply
{
    private ClientData client;
    private String     clientSecret;

    public
    CreateClientReply()
    {
        super();
        client = null;
        clientSecret = null;
    }

    public
    CreateClientReply(CreateClientRequest originatingRequest)
    {
        super(originatingRequest);
        client = null;
        clientSecret = null;
    }

    @Override
    public CreateClientReply
    setReplyId(UUID replyId)
    {
        return (CreateClientReply)super.setReplyId(replyId);
    }

    @Override
    public CreateClientReply
    setOriginatingRequestId(UUID requestId)
    {
        return (CreateClientReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public CreateClientReply
    setTimestamp(Instant timestamp)
    {
        return (CreateClientReply)super.setTimestamp(timestamp);
    }

    @Override
    public CreateClientReply
    setSuccess(boolean success)
    {
        return (CreateClientReply)super.setSuccess(success);
    }

    @Override
    public CreateClientReply
    setSuccessMessage(String successMessage)
    {
        return (CreateClientReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public CreateClientReply
    setFailureMessage(String failureMessage)
    {
        return (CreateClientReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public CreateClientReply
    setException(ExceptionData exception)
    {
        return (CreateClientReply)super.setException(exception);
    }

    public CreateClientReply
    setClient(ClientData value)
    {
        client = value;
        return this;
    }

    public CreateClientReply
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
        return "CreateClientReply{replyId=" + getReplyId() + ",success=" + isSuccess() + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
