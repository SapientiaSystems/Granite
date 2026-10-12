//////////////////////////////////////////////////////////////////////////////
// SessionTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionTest
{
    @Test
    public void
    testUserIdDefaultsToNull()
    {
        assertNull(new Session().getUserId());
    }

    @Test
    public void
    testAccessTokenDefaultsToNull()
    {
        assertNull(new Session().getAccessToken());
    }

    @Test
    public void
    testExpiresDefaultsToNull()
    {
        assertNull(new Session().getExpires());
    }

    @Test
    public void
    testDeviceIdDefaultsToNull()
    {
        assertNull(new Session().getDeviceId());
    }

    @Test
    public void
    testValuesDefaultsToEmptyMap()
    {
        Session target = new Session();

        assertNotNull(target.getValues());
        assertTrue(target.getValues().isEmpty());
    }

    @Test
    public void
    testSetUserIdFluentlyAndRead()
    {
        Session target = new Session();

        assertSame(target,target.setUserId(42L));
        assertEquals(42L,target.getUserId());
    }

    @Test
    public void
    testSetAccessTokenFluentlyAndRead()
    {
        Session target = new Session();

        assertSame(target,target.setAccessToken("jwt.abc.def"));
        assertEquals("jwt.abc.def",target.getAccessToken());
    }

    @Test
    public void
    testSetDeviceIdFluentlyAndRead()
    {
        Session target = new Session();

        assertSame(target,target.setDeviceId("device-123"));
        assertEquals("device-123",target.getDeviceId());
    }

    @Test
    public void
    testSetSessionIdFluentlyAndRead()
    {
        Session target = new Session();

        assertSame(target,target.setSessionId(99L));
        assertEquals(99L,target.getSessionId());
        assertEquals(99L,target.getPrimaryId());
    }

    @Test
    public void
    testSetExpiresFiresSessionUpdated()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.setExpires(Instant.parse("2026-10-06T12:00:00Z"));

        assertEquals(1,observer.events.size());
        assertTrue(observer.events.get(0) instanceof SessionUpdated);
        assertEquals("SessionUpdated",observer.events.get(0).getName());
    }

    @Test
    public void
    testSetValueFluentlyAndReadByType()
    {
        Session target = new Session();

        assertSame(target,target.setValue("cart-id","c-1"));
        assertEquals(Optional.of("c-1"),target.getValue(String.class,"cart-id"));
    }

    @Test
    public void
    testSetValueFiresSessionUpdated()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.setValue("cart-id","c-1");

        assertEquals(1,observer.events.size());
        assertTrue(observer.events.get(0) instanceof SessionUpdated);
    }

    @Test
    public void
    testGetValueOnMissingKeyReturnsEmpty()
    {
        assertTrue(new Session().getValue(String.class,"missing").isEmpty());
    }

    @Test
    public void
    testGetValueOnTypeMismatchReturnsEmpty()
    {
        Session target = new Session().setValue("count","not-a-number");

        assertTrue(target.getValue(Integer.class,"count").isEmpty());
    }

    @Test
    public void
    testHasValueReturnsTrueForPresentKey()
    {
        Session target = new Session().setValue("cart-id","c-1");

        assertTrue(target.hasValue("cart-id"));
    }

    @Test
    public void
    testHasValueReturnsFalseForAbsentKey()
    {
        assertFalse(new Session().hasValue("missing"));
    }

    @Test
    public void
    testDestroyFiresSessionDestroyed()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.destroy();

        assertEquals(1,observer.events.size());
        assertTrue(observer.events.get(0) instanceof SessionDestroyed);
        assertEquals("SessionDestroyed",observer.events.get(0).getName());
    }

    @Test
    public void
    testNotifyCreatedFiresSessionCreated()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.notifyCreated();

        assertEquals(1,observer.events.size());
        assertTrue(observer.events.get(0) instanceof SessionCreated);
        assertEquals("SessionCreated",observer.events.get(0).getName());
    }

    @Test
    public void
    testEventSourceReportsSessionAsSource()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.notifyCreated();

        assertSame(target,observer.events.get(0).getSource());
    }
}

//////////////////////////////////////////////////////////////////////////////
