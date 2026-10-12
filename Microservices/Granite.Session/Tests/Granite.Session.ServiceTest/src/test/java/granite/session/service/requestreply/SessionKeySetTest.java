//////////////////////////////////////////////////////////////////////////////
// SessionKeySetTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionKeySetTest
{
    @Test
    void
    sessionKeySetValuesAsLongsConverts()
    {
        SessionKeySet ids = SessionKeySet.ofSessionIds(Set.of(1L,2L,3L));

        assertEquals("sessionIds",ids.getName());
        assertEquals(Set.of("1","2","3"),ids.getValues());
        assertEquals(Set.of(1L,2L,3L),ids.getValuesAsLongs());
        assertEquals(Set.of(4L),SessionKeySet.ofUserIds(Set.of(4L)).getValuesAsLongs());
    }

    @Test
    void
    sessionKeySetFactoriesAndValidation()
        throws Exception
    {
        SessionKeySet tokens = SessionKeySet.ofAccessTokens(Set.of("a","b"));

        assertEquals("accessTokens",tokens.getName());
        assertEquals(Set.of("a","b"),tokens.getValues());
        assertTrue(SessionKeySet.empty(SessionKeySet.USER_IDS).isEmpty());
        assertThrows(IllegalArgumentException.class,() -> SessionKeySet.empty("bogus"));

        ObjectMapper mapper = new ObjectMapper();

        assertEquals(
            tokens,
            mapper.readValue(mapper.writeValueAsString(tokens),SessionKeySet.class));
    }
}

//////////////////////////////////////////////////////////////////////////////
