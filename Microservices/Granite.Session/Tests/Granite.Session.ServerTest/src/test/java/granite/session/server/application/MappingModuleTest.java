//////////////////////////////////////////////////////////////////////////////
// MappingModuleTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.LoginMethod;
import granite.session.server.domain.Session;
import granite.session.service.requestreply.SessionClosedReason;
import granite.session.service.requestreply.SessionData;
import granite.session.service.requestreply.SessionLoginMethod;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("CommitStage")
public
class MappingModuleTest
{
    @Test
    public void
    mapsLifecycleFields()
    {
        UUID        cid     = UUID.randomUUID();
        Instant     at      = Instant.parse("2026-10-06T12:00:00Z");
        Session     session =
            new Session()
                .setSessionId(8L)
                .setUserId(1L)
                .setDeviceId("device-A")
                .setExpires(at.plusSeconds(60))
                .setContinuityId(cid)
                .setReplacedBy(9L)
                .setCopiedFrom(7L)
                .close(SessionClosedReason.REPLACED,at);
        SessionData data    = newMapper().map(session,SessionData.class);

        assertEquals(cid,data.getContinuityId());
        assertEquals(SessionClosedReason.REPLACED,data.getClosedReason());
        assertEquals(9L,data.getReplacedBy());
        assertEquals(7L,data.getCopiedFrom());
        assertEquals(at,data.getExpires());
    }

    @Test
    public void
    mapsOpenSessionWithoutReason()
    {
        SessionData data =
            newMapper()
                .map(
                    new Session()
                        .setSessionId(8L)
                        .setUserId(1L)
                        .setDeviceId("device-A"),
                    SessionData.class);

        assertNull(data.getClosedReason());
        assertNull(data.getContinuityId());
        assertNull(data.getLastActivity());
        assertNull(data.getLoginMethod());
    }

    @Test
    public void
    mapsLastActivityAndLoginMethod()
    {
        Instant     at   = Instant.parse("2026-10-06T12:00:00Z");
        SessionData data =
            newMapper()
                .map(
                    new Session()
                        .setSessionId(8L)
                        .setUserId(1L)
                        .setDeviceId("device-A")
                        .setLastActivity(at)
                        .setLoginMethod(LoginMethod.OKTA),
                    SessionData.class);

        assertEquals(at,data.getLastActivity());
        assertEquals(SessionLoginMethod.OKTA,data.getLoginMethod());
    }

    private ModelMapper
    newMapper()
    {
        return new ModelMapper().registerModule(new MappingModule());
    }
}

//////////////////////////////////////////////////////////////////////////////
