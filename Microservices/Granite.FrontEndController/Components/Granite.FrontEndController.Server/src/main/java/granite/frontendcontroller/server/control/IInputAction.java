//////////////////////////////////////////////////////////////////////////////
// IInputAction.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control;

import strata.frontendcontroller.service.requestreply.FrontEndInput;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.concurrent.CompletionStage;

public
interface IInputAction<I extends FrontEndInput,O extends FrontEndOutput>
{
    CompletionStage<O>
    apply(I input);
}

//////////////////////////////////////////////////////////////////////////////
