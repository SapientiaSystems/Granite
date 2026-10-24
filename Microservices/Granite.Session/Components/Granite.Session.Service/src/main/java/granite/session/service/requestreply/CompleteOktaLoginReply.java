//////////////////////////////////////////////////////////////////////////////
// CompleteOktaLoginReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class CompleteOktaLoginReply
    extends AbstractServiceReply
{
    private SessionData sessionData;
    private String      sessionHandle;
    private String      userName;
    private String      returnTo;

    public
    CompleteOktaLoginReply()
    {
        super();
        sessionData   = null;
        sessionHandle = null;
        userName      = null;
        returnTo      = null;
    }

    public
    CompleteOktaLoginReply(CompleteOktaLoginRequest originatingRequest)
    {
        super(originatingRequest);
        sessionData   = null;
        sessionHandle = null;
        userName      = null;
        returnTo      = null;
    }

    @Override
    public CompleteOktaLoginReply
    setReplyId(UUID replyId)
    {
        return (CompleteOktaLoginReply)super.setReplyId(replyId);
    }

    @Override
    public CompleteOktaLoginReply
    setOriginatingRequestId(UUID requestId)
    {
        return (CompleteOktaLoginReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public CompleteOktaLoginReply
    setTimestamp(Instant timestamp)
    {
        return (CompleteOktaLoginReply)super.setTimestamp(timestamp);
    }

    @Override
    public CompleteOktaLoginReply
    setSuccess(boolean success)
    {
        return (CompleteOktaLoginReply)super.setSuccess(success);
    }

    @Override
    public CompleteOktaLoginReply
    setSuccessMessage(String successMessage)
    {
        return (CompleteOktaLoginReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public CompleteOktaLoginReply
    setFailureMessage(String failureMessage)
    {
        return (CompleteOktaLoginReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public CompleteOktaLoginReply
    setException(ExceptionData exception)
    {
        return (CompleteOktaLoginReply)super.setException(exception);
    }

    public CompleteOktaLoginReply
    setSessionData(SessionData sessionData)
    {
        this.sessionData = sessionData;
        return this;
    }

    public CompleteOktaLoginReply
    setSessionHandle(String sessionHandle)
    {
        this.sessionHandle = sessionHandle;
        return this;
    }

    public CompleteOktaLoginReply
    setUserName(String userName)
    {
        this.userName = userName;
        return this;
    }

    public CompleteOktaLoginReply
    setReturnTo(String returnTo)
    {
        this.returnTo = returnTo;
        return this;
    }

    public SessionData
    getSessionData() { return sessionData; }

    public String
    getSessionHandle() { return sessionHandle; }

    public String
    getUserName() { return userName; }

    public String
    getReturnTo() { return returnTo; }

    @Override
    public String
    toString()
    {
        return
            "CompleteOktaLoginReply{replyId=" + getReplyId()
                + ",success=" + isSuccess()
                + ",sessionId=" + (sessionData != null ? sessionData.getSessionId() : null)
                + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
