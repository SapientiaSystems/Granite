//////////////////////////////////////////////////////////////////////////////
// ContentOutputs.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.content;

import granite.frontendcontroller.service.requestreply.ContentInput;
import granite.frontendcontroller.service.requestreply.ContentOutput;

public
class ContentOutputs
{
    private
    ContentOutputs() {}

    public static ContentOutput
    describe(ContentInput input,ContentOutput output,Long caller)
    {
        return
            output
                .setUserId(caller)
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }

    public static String
    reason(String message)
    {
        return message == null || message.isBlank() ? "unknown error" : message;
    }
}

//////////////////////////////////////////////////////////////////////////////
