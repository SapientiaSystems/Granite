//////////////////////////////////////////////////////////////////////////////
// SessionKeyTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CommitStage")
public
class SessionKeyTest
{
    @Test
    void
    sessionKeyAcceptsOnlyKnownNames()
    {
        assertEquals(SessionKey.SESSION_ID,SessionKey.of(SessionKey.SESSION_ID,"1").getName());
        assertEquals(SessionKey.ACCESS_TOKEN,SessionKey.of(SessionKey.ACCESS_TOKEN,"t").getName());
        assertEquals(SessionKey.USER_ID,SessionKey.of(SessionKey.USER_ID,"2").getName());
        assertThrows(IllegalArgumentException.class,() -> SessionKey.of("bogus","1"));
    }

    @Test
    void
    sessionKeyFactoriesSetCorrectNameAndStringValue()
    {
        SessionKey sessionId = SessionKey.ofSessionId(42L);
        SessionKey token     = SessionKey.ofAccessToken("abc");
        SessionKey userId    = SessionKey.ofUserId(7L);

        assertEquals("sessionId",sessionId.getName());
        assertEquals("42",sessionId.getValue());
        assertEquals(42L,sessionId.getValueAsLong());
        assertEquals("accessToken",token.getName());
        assertEquals("abc",token.getValue());
        assertEquals("userId",userId.getName());
        assertEquals("7",userId.getValue());
        assertEquals(7L,userId.getValueAsLong());
    }

    @Test
    void
    sessionKeyEqualsAndHashCode()
    {
        assertEquals(SessionKey.ofSessionId(1L),SessionKey.ofSessionId(1L));
        assertEquals(SessionKey.ofSessionId(1L).hashCode(),SessionKey.ofSessionId(1L).hashCode());
        assertNotEquals(SessionKey.ofSessionId(1L),SessionKey.ofSessionId(2L));
        assertNotEquals(SessionKey.ofSessionId(1L),SessionKey.ofUserId(1L));
    }

    @Test
    void
    sessionKeyJsonRoundTrip()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SessionKey   key    = SessionKey.ofAccessToken("abc");
        String       json   = mapper.writeValueAsString(key);

        assertEquals(key,mapper.readValue(json,SessionKey.class));
    }
}

//////////////////////////////////////////////////////////////////////////////
