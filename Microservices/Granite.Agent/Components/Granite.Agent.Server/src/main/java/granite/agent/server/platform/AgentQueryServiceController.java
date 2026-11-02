//////////////////////////////////////////////////////////////////////////////
// AgentQueryServiceController.java
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
@RequestMapping("/agent-query-service")
@EnableTransactionManagement
public
class AgentQueryServiceController
{
    private final IAgentQueryService implementation;
    private final Logger              logger;

    @Inject
    public
    AgentQueryServiceController(IAgentQueryService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(AgentQueryServiceController.class);
    }

    @PostMapping(value = "/find-agent-conversations",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindAgentConversationsReply>
    findAgentConversations(@RequestBody FindAgentConversationsRequest request)
    {
        logger.info("delegating findAgentConversations to implementation service");
        return implementation.findAgentConversations(request);
    }

    @PostMapping(value = "/find-agent-conversation",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindAgentConversationReply>
    findAgentConversation(@RequestBody FindAgentConversationRequest request)
    {
        logger.info("delegating findAgentConversation to implementation service");
        return implementation.findAgentConversation(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
