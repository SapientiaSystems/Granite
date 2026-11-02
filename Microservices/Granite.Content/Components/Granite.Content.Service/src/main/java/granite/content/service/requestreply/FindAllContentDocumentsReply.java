//////////////////////////////////////////////////////////////////////////////
// FindAllContentDocumentsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class FindAllContentDocumentsReply
    extends AbstractServiceReply
{
    private List<ContentDocumentData> documents;

    public
    FindAllContentDocumentsReply()
    {
        super();
        documents = new ArrayList<>();
    }

    public
    FindAllContentDocumentsReply(FindAllContentDocumentsRequest originatingRequest)
    {
        super(originatingRequest);
        documents = new ArrayList<>();
    }

    @Override
    public FindAllContentDocumentsReply
    setReplyId(UUID replyId)
    {
        return (FindAllContentDocumentsReply)super.setReplyId(replyId);
    }

    @Override
    public FindAllContentDocumentsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindAllContentDocumentsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindAllContentDocumentsReply
    setTimestamp(Instant timestamp)
    {
        return (FindAllContentDocumentsReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindAllContentDocumentsReply
    setSuccess(boolean success)
    {
        return (FindAllContentDocumentsReply)super.setSuccess(success);
    }

    @Override
    public FindAllContentDocumentsReply
    setSuccessMessage(String successMessage)
    {
        return (FindAllContentDocumentsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindAllContentDocumentsReply
    setFailureMessage(String failureMessage)
    {
        return (FindAllContentDocumentsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindAllContentDocumentsReply
    setException(ExceptionData exception)
    {
        return (FindAllContentDocumentsReply)super.setException(exception);
    }

    public FindAllContentDocumentsReply
    setDocuments(List<ContentDocumentData> value)
    {
        documents = value;
        return this;
    }

    public List<ContentDocumentData>
    getDocuments() { return documents; }
}

//////////////////////////////////////////////////////////////////////////////
