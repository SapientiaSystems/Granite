//////////////////////////////////////////////////////////////////////////////
// SessionLeakTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("CommitStage")
public
class SessionLeakTest
{
    @Test
    public void
    noSecretInToStringOrOutput()
    {
        String text =
            new SessionInput().setUserName("amy").setPassword(SessionTestSupport.PASSWORD)
                + new SessionOutput().setUserName("amy").setAuthorities(Set.of("ROLE_User")).setStatus("AUTHENTICATED").toString()
                + new SessionOutput().toString()
                + new SessionLoginSettings(true,false).toString();

        assertFalse(text.contains(SessionTestSupport.PASSWORD));
        assertFalse(text.contains(SessionTestSupport.HANDLE));
    }
}

//////////////////////////////////////////////////////////////////////////////
