//////////////////////////////////////////////////////////////////////////////
// SessionRowData.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public
class SessionRowData
    implements Serializable
{
    private Long               sessionId;
    private Long               userId;
    private String             userName;
    private List<String>       authorities;
    private String             deviceId;
    private String             loginMethod;
    private String             created;
    private String             lastActivity;
    private String             expires;
    private String             closedReason;
    private String             continuityId;
    private String             application;
    private Long               replacedBy;
    private Long               copiedFrom;
    private Map<String,Object> values;

    public
    SessionRowData()
    {
        sessionId    = null;
        userId       = null;
        userName     = null;
        authorities  = new ArrayList<>();
        deviceId     = null;
        loginMethod  = null;
        created      = null;
        lastActivity = null;
        expires      = null;
        closedReason = null;
        continuityId = null;
        application  = null;
        replacedBy   = null;
        copiedFrom   = null;
        values       = new LinkedHashMap<>();
    }

    public SessionRowData
    setSessionId(Long id)
    {
        sessionId = id;
        return this;
    }

    public SessionRowData
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public SessionRowData
    setUserName(String name)
    {
        userName = name;
        return this;
    }

    public SessionRowData
    setAuthorities(List<String> values)
    {
        authorities = values;
        return this;
    }

    public SessionRowData
    setDeviceId(String id)
    {
        deviceId = id;
        return this;
    }

    public SessionRowData
    setLoginMethod(String method)
    {
        loginMethod = method;
        return this;
    }

    public SessionRowData
    setCreated(String time)
    {
        created = time;
        return this;
    }

    public SessionRowData
    setLastActivity(String time)
    {
        lastActivity = time;
        return this;
    }

    public SessionRowData
    setExpires(String time)
    {
        expires = time;
        return this;
    }

    public SessionRowData
    setClosedReason(String reason)
    {
        closedReason = reason;
        return this;
    }

    public SessionRowData
    setApplication(String name)
    {
        application = name;
        return this;
    }

    public SessionRowData
    setContinuityId(String id)
    {
        continuityId = id;
        return this;
    }

    public SessionRowData
    setReplacedBy(Long id)
    {
        replacedBy = id;
        return this;
    }

    public SessionRowData
    setCopiedFrom(Long id)
    {
        copiedFrom = id;
        return this;
    }

    public SessionRowData
    setValues(Map<String,Object> v)
    {
        values = v;
        return this;
    }

    public Long
    getSessionId() { return sessionId; }

    public Long
    getUserId() { return userId; }

    public String
    getUserName() { return userName; }

    public List<String>
    getAuthorities() { return authorities; }

    public String
    getDeviceId() { return deviceId; }

    public String
    getLoginMethod() { return loginMethod; }

    public String
    getCreated() { return created; }

    public String
    getLastActivity() { return lastActivity; }

    public String
    getExpires() { return expires; }

    public String
    getClosedReason() { return closedReason; }

    public String
    getContinuityId() { return continuityId; }

    public String
    getApplication() { return application; }

    public Long
    getReplacedBy() { return replacedBy; }

    public Long
    getCopiedFrom() { return copiedFrom; }

    public Map<String,Object>
    getValues() { return values; }
}

//////////////////////////////////////////////////////////////////////////////
