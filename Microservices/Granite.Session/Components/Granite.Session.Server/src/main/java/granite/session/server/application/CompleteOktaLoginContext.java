//////////////////////////////////////////////////////////////////////////////
// CompleteOktaLoginContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.OktaLoginTransaction;
import granite.session.server.domain.Session;
import granite.session.service.requestreply.CompleteOktaLoginRequest;

import java.util.LinkedHashSet;
import java.util.Set;

public
class CompleteOktaLoginContext
{
    private final CompleteOktaLoginRequest request;
    private String                         hashedTransactionId;
    private OktaLoginTransaction           transaction;
    private OktaIdentity                   identity;
    private Long                           userId;
    private String                         userName;
    private Set<String>                    authorities;
    private LoginLifecycle                 lifecycle;
    private Session                        created;
    private String                         sessionHandle;
    private String                         failureReason;

    public
    CompleteOktaLoginContext(CompleteOktaLoginRequest request)
    {
        this.request        = request;
        hashedTransactionId = null;
        transaction         = null;
        identity            = null;
        userId              = null;
        userName            = null;
        authorities         = new LinkedHashSet<>();
        lifecycle           = null;
        created             = null;
        sessionHandle       = null;
        failureReason       = null;
    }

    public CompleteOktaLoginContext
    setHashedTransactionId(String value)
    {
        hashedTransactionId = value;
        return this;
    }

    public CompleteOktaLoginContext
    setTransaction(OktaLoginTransaction value)
    {
        transaction = value;
        return this;
    }

    public CompleteOktaLoginContext
    setIdentity(OktaIdentity value)
    {
        identity = value;
        return this;
    }

    public CompleteOktaLoginContext
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public CompleteOktaLoginContext
    setUserName(String value)
    {
        userName = value;
        return this;
    }

    public CompleteOktaLoginContext
    setAuthorities(Set<String> values)
    {
        authorities = values != null ? new LinkedHashSet<>(values) : new LinkedHashSet<>();
        return this;
    }

    public CompleteOktaLoginContext
    setLifecycle(LoginLifecycle value)
    {
        lifecycle = value;
        return this;
    }

    public CompleteOktaLoginContext
    setCreated(Session value)
    {
        created = value;
        return this;
    }

    public CompleteOktaLoginContext
    setSessionHandle(String value)
    {
        sessionHandle = value;
        return this;
    }

    public CompleteOktaLoginContext
    reject(String reason)
    {
        if (failureReason == null)
            failureReason = reason;

        return this;
    }

    public CompleteOktaLoginRequest
    getRequest() { return request; }

    public String
    getCode() { return request.getCode(); }

    public String
    getState() { return request.getState(); }

    public String
    getTransactionId() { return request.getTransactionId(); }

    public String
    getApplication()
    {
        return transaction != null ? transaction.getApplication() : Session.DEFAULT_APPLICATION;
    }

    public String
    getDeviceId() { return request.getDeviceId(); }

    public String
    getHashedTransactionId() { return hashedTransactionId; }

    public OktaLoginTransaction
    getTransaction() { return transaction; }

    public OktaIdentity
    getIdentity() { return identity; }

    public Long
    getUserId() { return userId; }

    public String
    getUserName() { return userName; }

    public Set<String>
    getAuthorities() { return authorities; }

    public LoginLifecycle
    getLifecycle() { return lifecycle; }

    public Session
    getCreated() { return created; }

    public String
    getSessionHandle() { return sessionHandle; }

    public String
    getFailureReason() { return failureReason; }

    public boolean
    isRejected() { return failureReason != null; }
}

//////////////////////////////////////////////////////////////////////////////
