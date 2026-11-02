//////////////////////////////////////////////////////////////////////////////
// DestroyContentDocumentsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class DestroyContentDocumentsReply
    extends AbstractServiceReply
{
    private Set<String> requestedKeys;
    private Set<String> failedKeys;
    private Set<String> destroyedKeys;

    public
    DestroyContentDocumentsReply()
    {
        super();
        requestedKeys = new TreeSet<>();
        failedKeys    = new TreeSet<>();
        destroyedKeys = new TreeSet<>();
    }

    public
    DestroyContentDocumentsReply(DestroyContentDocumentsRequest originatingRequest)
    {
        super(originatingRequest);
        requestedKeys = new TreeSet<>();
        failedKeys    = new TreeSet<>();
        destroyedKeys = new TreeSet<>();
    }

    @Override
    public DestroyContentDocumentsReply
    setReplyId(UUID replyId)
    {
        return (DestroyContentDocumentsReply)super.setReplyId(replyId);
    }

    @Override
    public DestroyContentDocumentsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (DestroyContentDocumentsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public DestroyContentDocumentsReply
    setTimestamp(Instant timestamp)
    {
        return (DestroyContentDocumentsReply)super.setTimestamp(timestamp);
    }

    @Override
    public DestroyContentDocumentsReply
    setSuccess(boolean success)
    {
        return (DestroyContentDocumentsReply)super.setSuccess(success);
    }

    @Override
    public DestroyContentDocumentsReply
    setSuccessMessage(String successMessage)
    {
        return (DestroyContentDocumentsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public DestroyContentDocumentsReply
    setFailureMessage(String failureMessage)
    {
        return (DestroyContentDocumentsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public DestroyContentDocumentsReply
    setException(ExceptionData exception)
    {
        return (DestroyContentDocumentsReply)super.setException(exception);
    }

    public DestroyContentDocumentsReply
    setRequestedKeys(Set<String> value)
    {
        requestedKeys = value;
        return this;
    }

    public DestroyContentDocumentsReply
    setFailedKeys(Set<String> value)
    {
        failedKeys = value;
        return this;
    }

    public DestroyContentDocumentsReply
    setDestroyedKeys(Set<String> value)
    {
        destroyedKeys = value;
        return this;
    }

    public Set<String>
    getRequestedKeys() { return requestedKeys; }

    public Set<String>
    getFailedKeys() { return failedKeys; }

    public Set<String>
    getDestroyedKeys() { return destroyedKeys; }
}

//////////////////////////////////////////////////////////////////////////////
