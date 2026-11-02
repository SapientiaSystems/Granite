//////////////////////////////////////////////////////////////////////////////
// SearchContentReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class SearchContentReply
    extends AbstractServiceReply
{
    private List<ContentHitData> hits;

    public
    SearchContentReply()
    {
        super();
        hits = new ArrayList<>();
    }

    public
    SearchContentReply(SearchContentRequest originatingRequest)
    {
        super(originatingRequest);
        hits = new ArrayList<>();
    }

    @Override
    public SearchContentReply
    setReplyId(UUID replyId)
    {
        return (SearchContentReply)super.setReplyId(replyId);
    }

    @Override
    public SearchContentReply
    setOriginatingRequestId(UUID requestId)
    {
        return (SearchContentReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public SearchContentReply
    setTimestamp(Instant timestamp)
    {
        return (SearchContentReply)super.setTimestamp(timestamp);
    }

    @Override
    public SearchContentReply
    setSuccess(boolean success)
    {
        return (SearchContentReply)super.setSuccess(success);
    }

    @Override
    public SearchContentReply
    setSuccessMessage(String successMessage)
    {
        return (SearchContentReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public SearchContentReply
    setFailureMessage(String failureMessage)
    {
        return (SearchContentReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public SearchContentReply
    setException(ExceptionData exception)
    {
        return (SearchContentReply)super.setException(exception);
    }

    public SearchContentReply
    setHits(List<ContentHitData> value)
    {
        hits = value;
        return this;
    }

    public List<ContentHitData>
    getHits() { return hits; }
}

//////////////////////////////////////////////////////////////////////////////
