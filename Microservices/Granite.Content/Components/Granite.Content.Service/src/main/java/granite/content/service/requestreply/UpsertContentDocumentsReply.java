//////////////////////////////////////////////////////////////////////////////
// UpsertContentDocumentsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class UpsertContentDocumentsReply
    extends AbstractServiceReply
{
    private Set<String>               requestedKeys;
    private Set<String>               failedKeys;
    private List<ContentDocumentData> upsertedDocuments;

    public
    UpsertContentDocumentsReply()
    {
        super();
        requestedKeys     = new TreeSet<>();
        failedKeys        = new TreeSet<>();
        upsertedDocuments = new ArrayList<>();
    }

    public
    UpsertContentDocumentsReply(UpsertContentDocumentsRequest originatingRequest)
    {
        super(originatingRequest);
        requestedKeys     = new TreeSet<>();
        failedKeys        = new TreeSet<>();
        upsertedDocuments = new ArrayList<>();
    }

    @Override
    public UpsertContentDocumentsReply
    setReplyId(UUID replyId)
    {
        return (UpsertContentDocumentsReply)super.setReplyId(replyId);
    }

    @Override
    public UpsertContentDocumentsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (UpsertContentDocumentsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public UpsertContentDocumentsReply
    setTimestamp(Instant timestamp)
    {
        return (UpsertContentDocumentsReply)super.setTimestamp(timestamp);
    }

    @Override
    public UpsertContentDocumentsReply
    setSuccess(boolean success)
    {
        return (UpsertContentDocumentsReply)super.setSuccess(success);
    }

    @Override
    public UpsertContentDocumentsReply
    setSuccessMessage(String successMessage)
    {
        return (UpsertContentDocumentsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public UpsertContentDocumentsReply
    setFailureMessage(String failureMessage)
    {
        return (UpsertContentDocumentsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public UpsertContentDocumentsReply
    setException(ExceptionData exception)
    {
        return (UpsertContentDocumentsReply)super.setException(exception);
    }

    public UpsertContentDocumentsReply
    setRequestedKeys(Set<String> value)
    {
        requestedKeys = value;
        return this;
    }

    public UpsertContentDocumentsReply
    setFailedKeys(Set<String> value)
    {
        failedKeys = value;
        return this;
    }

    public UpsertContentDocumentsReply
    setUpsertedDocuments(List<ContentDocumentData> value)
    {
        upsertedDocuments = value;
        return this;
    }

    public Set<String>
    getRequestedKeys() { return requestedKeys; }

    public Set<String>
    getFailedKeys() { return failedKeys; }

    public List<ContentDocumentData>
    getUpsertedDocuments() { return upsertedDocuments; }
}

//////////////////////////////////////////////////////////////////////////////
