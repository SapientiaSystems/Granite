//////////////////////////////////////////////////////////////////////////////
// FindSessionByUserIdAndDeviceIdReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class FindSessionByUserIdAndDeviceIdReply
    extends AbstractServiceReply
{
    private SessionData session;

    public
    FindSessionByUserIdAndDeviceIdReply()
    {
        super();
        session = null;
    }

    public
    FindSessionByUserIdAndDeviceIdReply(FindSessionByUserIdAndDeviceIdRequest originatingRequest)
    {
        super(originatingRequest);
        session = null;
    }

    @Override
    public FindSessionByUserIdAndDeviceIdReply
    setReplyId(UUID replyId)
    {
        return (FindSessionByUserIdAndDeviceIdReply)super.setReplyId(replyId);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindSessionByUserIdAndDeviceIdReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdReply
    setTimestamp(Instant timestamp)
    {
        return (FindSessionByUserIdAndDeviceIdReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdReply
    setSuccess(boolean success)
    {
        return (FindSessionByUserIdAndDeviceIdReply)super.setSuccess(success);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdReply
    setSuccessMessage(String successMessage)
    {
        return (FindSessionByUserIdAndDeviceIdReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdReply
    setFailureMessage(String failureMessage)
    {
        return (FindSessionByUserIdAndDeviceIdReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindSessionByUserIdAndDeviceIdReply
    setException(ExceptionData exception)
    {
        return (FindSessionByUserIdAndDeviceIdReply)super.setException(exception);
    }

    public FindSessionByUserIdAndDeviceIdReply
    setSession(SessionData value)
    {
        session = value;
        return this;
    }

    public SessionData
    getSession() { return session; }
}

//////////////////////////////////////////////////////////////////////////////
