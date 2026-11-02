//////////////////////////////////////////////////////////////////////////////
// UnauthenticatedSessionsOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.sessions;

import granite.frontendcontroller.service.requestreply.SessionsOutput;

import java.util.ArrayList;
import java.util.List;

public
class UnauthenticatedSessionsOutput
{
    public static final String MESSAGE = "not authenticated";

    private
    UnauthenticatedSessionsOutput() {}

    public static SessionsOutput
    create()
    {
        return
            new SessionsOutput()
                .setSessions(List.of())
                .setCurrentSessionId(null)
                .setFailureMessages(new ArrayList<>(List.of(MESSAGE)));
    }
}

//////////////////////////////////////////////////////////////////////////////
