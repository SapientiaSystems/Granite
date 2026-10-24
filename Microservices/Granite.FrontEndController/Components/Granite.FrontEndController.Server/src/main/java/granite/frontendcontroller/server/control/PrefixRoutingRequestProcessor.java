//////////////////////////////////////////////////////////////////////////////
// PrefixRoutingRequestProcessor.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control;

import strata.frontendcontroller.server.application.IRequestProcessor;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.Map;
import java.util.concurrent.CompletionStage;

public
class PrefixRoutingRequestProcessor
    implements IRequestProcessor
{
    private final Map<String,IRequestProcessor> routes;
    private final IRequestProcessor             fallback;

    public
    PrefixRoutingRequestProcessor(Map<String,IRequestProcessor> routes,IRequestProcessor fallback)
    {
        this.routes = Map.copyOf(routes);
        this.fallback = fallback;
    }

    @Override
    public CompletionStage<FrontEndOutput>
    process(HandleInputRequest request)
    {
        return route(request.getInput() == null ? null : request.getInput().getSource()).process(request);
    }

    @Override
    public CompletionStage<FrontEndOutput>
    process(GetOutputRequest request)
    {
        return route(request.getSource()).process(request);
    }

    private IRequestProcessor
    route(String source)
    {
        if (source != null)
        {
            for (Map.Entry<String,IRequestProcessor> route : routes.entrySet())
            {
                if (source.startsWith(route.getKey()))
                    return route.getValue();
            }
        }

        return fallback;
    }
}

//////////////////////////////////////////////////////////////////////////////
