//////////////////////////////////////////////////////////////////////////////
// AgentCommandServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.platform;

import granite.agent.service.requestreply.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletionStage;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/agent-command-service")
@EnableTransactionManagement
public
class AgentCommandServiceController
{
    private final IAgentCommandService implementation;
    private final Logger                logger;

    @Inject
    public
    AgentCommandServiceController(IAgentCommandService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(AgentCommandServiceController.class);
    }

    @PostMapping(value = "/send-agent-message",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<SendAgentMessageReply>
    sendAgentMessage(@RequestBody SendAgentMessageRequest request)
    {
        logger.info("delegating sendAgentMessage to implementation service");
        return implementation.sendAgentMessage(request);
    }

    @PostMapping(value = "/resolve-agent-action",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<ResolveAgentActionReply>
    resolveAgentAction(@RequestBody ResolveAgentActionRequest request)
    {
        logger.info("delegating resolveAgentAction to implementation service");
        return implementation.resolveAgentAction(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
