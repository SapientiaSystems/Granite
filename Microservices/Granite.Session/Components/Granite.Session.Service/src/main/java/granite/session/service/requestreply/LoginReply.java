//////////////////////////////////////////////////////////////////////////////
// LoginReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class LoginReply
    extends AbstractServiceReply
{
    private SessionData sessionData;
    private String accessToken;
    private String refreshToken;
    private String sessionHandle;

    public
    LoginReply()
    {
        super();
        sessionData = null;
        accessToken = null;
        refreshToken = null;
        sessionHandle = null;
    }

    public
    LoginReply(LoginRequest originatingRequest)
    {
        super(originatingRequest);
        sessionData = null;
        accessToken = null;
        refreshToken = null;
        sessionHandle = null;
    }

    @Override
    public LoginReply
    setReplyId(UUID replyId)
    {
        return (LoginReply)super.setReplyId(replyId);
    }

    @Override
    public LoginReply
    setOriginatingRequestId(UUID requestId)
    {
        return (LoginReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public LoginReply
    setTimestamp(Instant timestamp)
    {
        return (LoginReply)super.setTimestamp(timestamp);
    }

    @Override
    public LoginReply
    setSuccess(boolean success)
    {
        return (LoginReply)super.setSuccess(success);
    }

    @Override
    public LoginReply
    setSuccessMessage(String successMessage)
    {
        return (LoginReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public LoginReply
    setFailureMessage(String failureMessage)
    {
        return (LoginReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public LoginReply
    setException(ExceptionData exception)
    {
        return (LoginReply)super.setException(exception);
    }

    public LoginReply
    setSessionData(SessionData sessionData)
    {
        this.sessionData = sessionData;
        return this;
    }

    public SessionData
    getSessionData() { return sessionData; }

    public LoginReply
    setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
        return this;
    }

    public String
    getAccessToken() { return accessToken; }

    public LoginReply
    setRefreshToken(String refreshToken)
    {
        this.refreshToken = refreshToken;
        return this;
    }

    public String
    getRefreshToken() { return refreshToken; }

    public LoginReply
    setSessionHandle(String sessionHandle)
    {
        this.sessionHandle = sessionHandle;
        return this;
    }

    public String
    getSessionHandle() { return sessionHandle; }
}

//////////////////////////////////////////////////////////////////////////////
