//////////////////////////////////////////////////////////////////////////////
// UserRequestProcessor.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import strata.frontendcontroller.server.application.IRequestProcessor;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.concurrent.CompletionStage;

public
class UserRequestProcessor
    implements IRequestProcessor
{
    @Override
    public CompletionStage<FrontEndOutput>
    process(HandleInputRequest handleInputRequest)
    {
        return null;
    }

    @Override
    public CompletionStage<FrontEndOutput>
    process(GetOutputRequest getOutputRequest)
    {
        return null;
    }
}

//////////////////////////////////////////////////////////////////////////////
