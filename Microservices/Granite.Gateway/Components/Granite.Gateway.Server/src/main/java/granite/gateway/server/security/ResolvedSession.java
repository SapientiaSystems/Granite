//////////////////////////////////////////////////////////////////////////////
// ResolvedSession.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import java.time.Instant;

public
class ResolvedSession
{
    private final String  accessToken;
    private final Instant cacheUntil;

    public
    ResolvedSession(String accessToken,Instant cacheUntil)
    {
        this.accessToken = accessToken;
        this.cacheUntil  = cacheUntil;
    }

    public String
    getAccessToken() { return accessToken; }

    public Instant
    getCacheUntil() { return cacheUntil; }

    public boolean
    isLiveAt(Instant now) { return cacheUntil.isAfter(now); }

    @Override
    public String
    toString() { return "ResolvedSession{cacheUntil=" + cacheUntil + "}"; }
}

//////////////////////////////////////////////////////////////////////////////
