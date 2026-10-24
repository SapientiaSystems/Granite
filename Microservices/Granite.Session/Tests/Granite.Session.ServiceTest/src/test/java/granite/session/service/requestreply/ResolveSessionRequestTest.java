//////////////////////////////////////////////////////////////////////////////
// ResolveSessionRequestTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ResolveSessionRequestTest
{
    private static final String HANDLE = "secret-handle-value-0123456789";

    @Test
    void
    handleIsFluentAndReadable()
    {
        ResolveSessionRequest target = new ResolveSessionRequest();

        assertNull(target.getHandle());
        assertSame(target,target.setHandle(HANDLE));
        assertEquals(HANDLE,target.getHandle());
    }

    @Test
    void
    baseSettersAreFluent()
    {
        ResolveSessionRequest target = new ResolveSessionRequest();
        UUID                  id     = UUID.randomUUID();

        assertSame(target,target.setRequestId(id));
        assertSame(target,target.addHeader("X-Test","1"));
        assertSame(target,target.clearHeaders());
        assertEquals(id,target.getRequestId());
    }

    @Test
    void
    toStringNeverContainsHandle()
    {
        UUID                  id     = UUID.randomUUID();
        ResolveSessionRequest target =
            new ResolveSessionRequest()
                .setRequestId(id)
                .setHandle(HANDLE);

        String text = target.toString();

        assertFalse(text.contains(HANDLE),"toString must not reveal the handle");
        assertTrue(text.contains(id.toString()));
        assertTrue(text.startsWith("ResolveSessionRequest"));
    }
}

//////////////////////////////////////////////////////////////////////////////
