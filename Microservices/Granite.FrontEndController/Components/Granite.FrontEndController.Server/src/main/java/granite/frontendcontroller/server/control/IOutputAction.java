//////////////////////////////////////////////////////////////////////////////
// IOutputAction.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control;

import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.util.concurrent.CompletionStage;

public
interface IOutputAction<R extends GetOutputRequest,O extends FrontEndOutput>
{
    CompletionStage<O>
    apply(R request);
}

//////////////////////////////////////////////////////////////////////////////
