//////////////////////////////////////////////////////////////////////////////
// UnauthenticatedClientsOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.frontendcontroller.service.requestreply.ClientsOutput;

import java.util.ArrayList;
import java.util.List;

public
class UnauthenticatedClientsOutput
{
    public static final String MESSAGE = "not authenticated";

    private
    UnauthenticatedClientsOutput() {}

    public static ClientsOutput
    create()
    {
        return new ClientsOutput().setFailureMessages(new ArrayList<>(List.of(MESSAGE)));
    }
}

//////////////////////////////////////////////////////////////////////////////
