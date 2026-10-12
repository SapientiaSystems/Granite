//////////////////////////////////////////////////////////////////////////////
// JwtVerificationResult.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public
class JwtVerificationResult
{
    private String       subject;
    private String       jwtId;
    private Instant      issuedAt;
    private Instant      expiresAt;
    private List<String> audience;
    private String       deviceId;

    public
    JwtVerificationResult()
    {
        subject   = null;
        jwtId     = null;
        issuedAt  = null;
        expiresAt = null;
        audience  = Collections.emptyList();
        deviceId  = null;
    }

    public JwtVerificationResult
    setSubject(String s)
    {
        subject = s;
        return this;
    }

    public String
    getSubject() { return subject; }

    public JwtVerificationResult
    setJwtId(String id)
    {
        jwtId = id;
        return this;
    }

    public String
    getJwtId() { return jwtId; }

    public JwtVerificationResult
    setIssuedAt(Instant time)
    {
        issuedAt = time;
        return this;
    }

    public Instant
    getIssuedAt() { return issuedAt; }

    public JwtVerificationResult
    setExpiresAt(Instant time)
    {
        expiresAt = time;
        return this;
    }

    public Instant
    getExpiresAt() { return expiresAt; }

    public JwtVerificationResult
    setAudience(List<String> aud)
    {
        audience = aud == null ? Collections.emptyList() : List.copyOf(aud);
        return this;
    }

    public List<String>
    getAudience() { return audience; }

    public JwtVerificationResult
    setDeviceId(String id)
    {
        deviceId = id;
        return this;
    }

    public String
    getDeviceId() { return deviceId; }
}

//////////////////////////////////////////////////////////////////////////////
