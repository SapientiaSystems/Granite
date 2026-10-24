//////////////////////////////////////////////////////////////////////////////
// StartOktaLoginContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.OktaLoginTransaction;
import granite.session.service.requestreply.StartOktaLoginRequest;

public
class StartOktaLoginContext
{
    private final StartOktaLoginRequest request;
    private String                      transactionId;
    private String                      codeChallenge;
    private boolean                     promptLogin;
    private OktaLoginTransaction        transaction;
    private String                      authorizeUrl;
    private boolean                     rejected;

    public
    StartOktaLoginContext(StartOktaLoginRequest request)
    {
        this.request  = request;
        transactionId = null;
        codeChallenge = null;
        promptLogin   = false;
        transaction   = null;
        authorizeUrl  = null;
        rejected      = false;
    }

    public StartOktaLoginContext
    setTransactionId(String value)
    {
        transactionId = value;
        return this;
    }

    public StartOktaLoginContext
    setCodeChallenge(String value)
    {
        codeChallenge = value;
        return this;
    }

    public StartOktaLoginContext
    setPromptLogin(boolean value)
    {
        promptLogin = value;
        return this;
    }

    public StartOktaLoginContext
    setTransaction(OktaLoginTransaction value)
    {
        transaction = value;
        return this;
    }

    public StartOktaLoginContext
    setAuthorizeUrl(String value)
    {
        authorizeUrl = value;
        return this;
    }

    public StartOktaLoginContext
    setRejected(boolean value)
    {
        rejected = value;
        return this;
    }

    public StartOktaLoginRequest
    getRequest() { return request; }

    public String
    getDeviceId() { return request.getDeviceId(); }

    public String
    getReturnTo() { return request.getReturnTo(); }

    public String
    getTransactionId() { return transactionId; }

    public String
    getCodeChallenge() { return codeChallenge; }

    public boolean
    isPromptLogin() { return promptLogin; }

    public OktaLoginTransaction
    getTransaction() { return transaction; }

    public String
    getAuthorizeUrl() { return authorizeUrl; }

    public boolean
    isRejected() { return rejected; }
}

//////////////////////////////////////////////////////////////////////////////
