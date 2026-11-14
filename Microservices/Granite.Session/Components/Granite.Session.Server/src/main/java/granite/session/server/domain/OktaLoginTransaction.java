//////////////////////////////////////////////////////////////////////////////
// OktaLoginTransaction.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import strata.server.core.entity.AbstractEntity;

import java.time.Instant;

public
class OktaLoginTransaction
    extends AbstractEntity<Long,OktaLoginTransaction>
{
    private String  hashedTransactionId;
    private String  state;
    private String  nonce;
    private String  codeVerifier;
    private String  returnTo;
    private String  deviceId;
    private Instant expires;
    private String  application;

    public
    OktaLoginTransaction()
    {
        super();
        hashedTransactionId = null;
        state               = null;
        nonce               = null;
        codeVerifier        = null;
        returnTo            = null;
        deviceId            = null;
        expires             = null;
        application         = Session.DEFAULT_APPLICATION;
    }

    public OktaLoginTransaction
    setHashedTransactionId(String hash)
    {
        hashedTransactionId = hash;
        return this;
    }

    public OktaLoginTransaction
    setState(String value)
    {
        state = value;
        return this;
    }

    public OktaLoginTransaction
    setNonce(String value)
    {
        nonce = value;
        return this;
    }

    public OktaLoginTransaction
    setCodeVerifier(String value)
    {
        codeVerifier = value;
        return this;
    }

    public OktaLoginTransaction
    setReturnTo(String value)
    {
        returnTo = value;
        return this;
    }

    public OktaLoginTransaction
    setDeviceId(String value)
    {
        deviceId = value;
        return this;
    }

    public OktaLoginTransaction
    setExpires(Instant value)
    {
        expires = value;
        return this;
    }

    public OktaLoginTransaction
    setApplication(String value)
    {
        application = value == null || value.isBlank() ? Session.DEFAULT_APPLICATION : value;
        return this;
    }

    public String
    getApplication() { return application; }

    public String
    getHashedTransactionId() { return hashedTransactionId; }

    public String
    getState() { return state; }

    public String
    getNonce() { return nonce; }

    public String
    getCodeVerifier() { return codeVerifier; }

    public String
    getReturnTo() { return returnTo; }

    public String
    getDeviceId() { return deviceId; }

    public Instant
    getExpires() { return expires; }

    public boolean
    isExpired(Instant now)
    {
        return expires == null || !expires.isAfter(now);
    }

    @Override
    public String
    toString()
    {
        return "OktaLoginTransaction{id=" + getPrimaryId() + ",expires=" + expires + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
