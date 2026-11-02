//////////////////////////////////////////////////////////////////////////////
// FindContentDocumentsByKeysReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class FindContentDocumentsByKeysReply
    extends AbstractServiceReply
{
    private List<ContentDocumentData> documents;

    public
    FindContentDocumentsByKeysReply()
    {
        super();
        documents = new ArrayList<>();
    }

    public
    FindContentDocumentsByKeysReply(FindContentDocumentsByKeysRequest originatingRequest)
    {
        super(originatingRequest);
        documents = new ArrayList<>();
    }

    @Override
    public FindContentDocumentsByKeysReply
    setReplyId(UUID replyId)
    {
        return (FindContentDocumentsByKeysReply)super.setReplyId(replyId);
    }

    @Override
    public FindContentDocumentsByKeysReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindContentDocumentsByKeysReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindContentDocumentsByKeysReply
    setTimestamp(Instant timestamp)
    {
        return (FindContentDocumentsByKeysReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindContentDocumentsByKeysReply
    setSuccess(boolean success)
    {
        return (FindContentDocumentsByKeysReply)super.setSuccess(success);
    }

    @Override
    public FindContentDocumentsByKeysReply
    setSuccessMessage(String successMessage)
    {
        return (FindContentDocumentsByKeysReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindContentDocumentsByKeysReply
    setFailureMessage(String failureMessage)
    {
        return (FindContentDocumentsByKeysReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindContentDocumentsByKeysReply
    setException(ExceptionData exception)
    {
        return (FindContentDocumentsByKeysReply)super.setException(exception);
    }

    public FindContentDocumentsByKeysReply
    setDocuments(List<ContentDocumentData> value)
    {
        documents = value;
        return this;
    }

    public List<ContentDocumentData>
    getDocuments() { return documents; }
}

//////////////////////////////////////////////////////////////////////////////
