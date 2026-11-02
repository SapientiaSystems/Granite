//////////////////////////////////////////////////////////////////////////////
// UnauthenticatedContentOutputs.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.content;

import granite.frontendcontroller.service.requestreply.ContentOutput;

import java.util.ArrayList;
import java.util.List;

public
class UnauthenticatedContentOutputs
{
    public static final String MESSAGE = "not authenticated";

    private
    UnauthenticatedContentOutputs() {}

    public static ContentOutput
    content()
    {
        return new ContentOutput().setFailureMessages(new ArrayList<>(List.of(MESSAGE)));
    }
}

//////////////////////////////////////////////////////////////////////////////
