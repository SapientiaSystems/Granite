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
        SessionKeySet userIds = SessionKeySet.ofUserIds(Set.of(5L,6L));

        assertEquals("userIds",userIds.getName());
        assertEquals(Set.of("5","6"),userIds.getValues());
        assertTrue(SessionKeySet.empty(SessionKeySet.USER_IDS).isEmpty());
        assertThrows(IllegalArgumentException.class,() -> SessionKeySet.empty("bogus"));
        assertThrows(IllegalArgumentException.class,() -> SessionKeySet.empty("accessTokens"));

        ObjectMapper mapper = new ObjectMapper();

        assertEquals(
            userIds,
            mapper.readValue(mapper.writeValueAsString(userIds),SessionKeySet.class));
    }
}

//////////////////////////////////////////////////////////////////////////////
