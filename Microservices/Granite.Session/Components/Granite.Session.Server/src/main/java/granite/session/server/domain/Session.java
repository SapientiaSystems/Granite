//////////////////////////////////////////////////////////////////////////////
// Session.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import strata.server.core.entity.AbstractEntityAndDomainEventSource;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public
class Session
    extends
        AbstractEntityAndDomainEventSource<
            Long,
            Session,
            ISessionEvent,
            ISessionEventObserver>
    implements ISessionEventSource
{
    private Long                userId;
    private String              accessToken;
    private Instant             expires;
    private String              deviceId;
    private Map<String,String>  values;

    public
    Session()
    {
        super();
        userId      = null;
        accessToken = null;
        expires     = null;
        deviceId    = null;
        values      = new HashMap<>();
    }

    @Override
    public Session
    setPrimaryId(Long primaryId)
    {
        return super.setPrimaryId(primaryId);
    }

    @Override
    public Long
    getPrimaryId()
    {
        return super.getPrimaryId();
    }

    public Session
    setSessionId(Long id)
    {
        return super.setPrimaryId(id);
    }

    public Long
    getSessionId() { return super.getPrimaryId(); }

    public Session
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public Session
    setAccessToken(String token)
    {
        accessToken = token;
        return this;
    }

    public String
    getAccessToken() { return accessToken; }

    public Session
    setExpires(Instant time)
    {
        expires = time;
        notifyUpdated();
        return this;
    }

    public Instant
    getExpires() { return expires; }

    public Session
    setDeviceId(String id)
    {
        deviceId = id;
        return this;
    }

    public String
    getDeviceId() { return deviceId; }

    public Session
    setValues(Map<String,String> v)
    {
        values = v;
        notifyUpdated();
        return this;
    }

    public Map<String,String>
    getValues() { return Collections.unmodifiableMap(values); }

    public Session
    putValue(String key,String value)
    {
        values.put(key,value);
        return notifyUpdated();
    }

    public Session
    removeValue(String key)
    {
        values.remove(key);
        return notifyUpdated();
    }

    public Session
    destroy()
    {
        return notifyDestroyed();
    }

    public Session
    notifyCreated() { return super.notify(new SessionCreated(this)); }

    public Session
    notifyUpdated() { return super.notify(new SessionUpdated(this)); }

    public Session
    notifyDestroyed() { return super.notify(new SessionDestroyed(this)); }

    @Override
    protected Session
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
