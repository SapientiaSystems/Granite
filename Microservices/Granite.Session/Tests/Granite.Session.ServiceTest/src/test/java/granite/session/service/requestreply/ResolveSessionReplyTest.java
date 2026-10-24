//////////////////////////////////////////////////////////////////////////////
// ResolveSessionReplyTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ResolveSessionReplyTest
{
    private static final String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.secret.signature";

    @Test
    void
    propertiesAreFluentAndReadable()
    {
        ResolveSessionReply target  = new ResolveSessionReply();
        SessionData         session = new SessionData().setSessionId(42L);

        assertNull(target.getSession());
        assertNull(target.getAccessToken());
        assertSame(target,target.setSession(session));
        assertSame(target,target.setAccessToken(ACCESS_TOKEN));
        assertSame(target,target.setSuccess(true));
        assertSame(target,target.setFailureMessage("x"));
        assertSame(session,target.getSession());
        assertEquals(ACCESS_TOKEN,target.getAccessToken());
    }

    @Test
    void
    replyCarriesOriginatingRequestId()
    {
        ResolveSessionRequest request = new ResolveSessionRequest().setHandle("h");
        ResolveSessionReply   target  = new ResolveSessionReply(request);

        assertEquals(request.getRequestId(),target.getOriginatingRequestId());
    }

    @Test
    void
    toStringNeverContainsAccessToken()
    {
        ResolveSessionReply target =
            new ResolveSessionReply()
                .setSuccess(true)
                .setSession(new SessionData().setSessionId(42L))
                .setAccessToken(ACCESS_TOKEN);

        String text = target.toString();

        assertFalse(text.contains(ACCESS_TOKEN),"toString must not reveal the access token");
        assertTrue(text.startsWith("ResolveSessionReply"));
        assertTrue(text.contains("42"));
    }
}

//////////////////////////////////////////////////////////////////////////////
