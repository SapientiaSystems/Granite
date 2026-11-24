//////////////////////////////////////////////////////////////////////////////
// VerifyClientCredentialsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class VerifyClientCredentialsReply
    extends AbstractServiceReply
{
    private boolean     valid;
    private String      clientId;
    private Set<String> authorities;

    public
    VerifyClientCredentialsReply()
    {
        super();
        valid = false;
        clientId = null;
        authorities = new TreeSet<>();
    }

    public
    VerifyClientCredentialsReply(VerifyClientCredentialsRequest originatingRequest)
    {
        super(originatingRequest);
        valid = false;
        clientId = null;
        authorities = new TreeSet<>();
    }

    @Override
    public VerifyClientCredentialsReply
    setReplyId(UUID replyId)
    {
        return (VerifyClientCredentialsReply)super.setReplyId(replyId);
    }

    @Override
    public VerifyClientCredentialsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (VerifyClientCredentialsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public VerifyClientCredentialsReply
    setTimestamp(Instant timestamp)
    {
        return (VerifyClientCredentialsReply)super.setTimestamp(timestamp);
    }

    @Override
    public VerifyClientCredentialsReply
    setSuccess(boolean success)
    {
        return (VerifyClientCredentialsReply)super.setSuccess(success);
    }

    @Override
    public VerifyClientCredentialsReply
    setSuccessMessage(String successMessage)
    {
        return (VerifyClientCredentialsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public VerifyClientCredentialsReply
    setFailureMessage(String failureMessage)
    {
        return (VerifyClientCredentialsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public VerifyClientCredentialsReply
    setException(ExceptionData exception)
    {
        return (VerifyClientCredentialsReply)super.setException(exception);
    }

    public VerifyClientCredentialsReply
    setValid(boolean value)
    {
        valid = value;
        return this;
    }

    public VerifyClientCredentialsReply
    setClientId(String value)
    {
        clientId = value;
        return this;
    }

    public VerifyClientCredentialsReply
    setAuthorities(Set<String> value)
    {
        authorities = value;
        return this;
    }

    public boolean
    isValid() { return valid; }

    public String
    getClientId() { return clientId; }

    public Set<String>
    getAuthorities() { return authorities; }
}

//////////////////////////////////////////////////////////////////////////////
