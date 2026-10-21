//////////////////////////////////////////////////////////////////////////////
// LoginWithOauth2Reply.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class LoginWithOauth2Reply
    extends AbstractServiceReply
{
    private SessionData sessionData;
    private String accessToken;
    private String refreshToken;

    public
    LoginWithOauth2Reply()
    {
        super();
        sessionData = null;
        accessToken = null;
        refreshToken = null;
    }

    public
    LoginWithOauth2Reply(LoginWithOauth2Request originatingRequest)
    {
        super(originatingRequest);
        sessionData = null;
        accessToken = null;
        refreshToken = null;
    }

    @Override
    public LoginWithOauth2Reply
    setReplyId(UUID replyId)
    {
        return (LoginWithOauth2Reply)super.setReplyId(replyId);
    }

    @Override
    public LoginWithOauth2Reply
    setOriginatingRequestId(UUID requestId)
    {
        return (LoginWithOauth2Reply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public LoginWithOauth2Reply
    setTimestamp(Instant timestamp)
    {
        return (LoginWithOauth2Reply)super.setTimestamp(timestamp);
    }

    @Override
    public LoginWithOauth2Reply
    setSuccess(boolean success)
    {
        return (LoginWithOauth2Reply)super.setSuccess(success);
    }

    @Override
    public LoginWithOauth2Reply
    setSuccessMessage(String successMessage)
    {
        return (LoginWithOauth2Reply)super.setSuccessMessage(successMessage);
    }

    @Override
    public LoginWithOauth2Reply
    setFailureMessage(String failureMessage)
    {
        return (LoginWithOauth2Reply)super.setFailureMessage(failureMessage);
    }

    @Override
    public LoginWithOauth2Reply
    setException(ExceptionData exception)
    {
        return (LoginWithOauth2Reply)super.setException(exception);
    }

    public LoginWithOauth2Reply
    setSessionData(SessionData sessionData)
    {
        this.sessionData = sessionData;
        return this;
    }

    public SessionData
    getSessionData() { return sessionData; }

    public LoginWithOauth2Reply
    setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
        return this;
    }

    public String
    getAccessToken() { return accessToken; }

    public LoginWithOauth2Reply
    setRefreshToken(String refreshToken)
    {
        this.refreshToken = refreshToken;
        return this;
    }

    public String
    getRefreshToken() { return refreshToken; }
}

//////////////////////////////////////////////////////////////////////////////
