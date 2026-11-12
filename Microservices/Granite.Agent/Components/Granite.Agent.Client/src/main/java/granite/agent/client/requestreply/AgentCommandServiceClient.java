//////////////////////////////////////////////////////////////////////////////
// AgentCommandServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.client.requestreply;

import granite.agent.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.core.service.CountedClient;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

public
class AgentCommandServiceClient
    extends RestEasyRestClient
    implements IAgentCommandServiceClient
{
    public
    AgentCommandServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "agent-command-service/");
    }

    public
    AgentCommandServiceClient(CountedClient client,String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "agent-command-service/");
    }

    @Override
    public CompletionStage<SendAgentMessageReply>
    sendAgentMessage(SendAgentMessageRequest request)
    {
        return
            doPostAsync(
                "send-agent-message",
                SendAgentMessageReply.class,
                request);
    }

    @Override
    public CompletionStage<ResolveAgentActionReply>
    resolveAgentAction(ResolveAgentActionRequest request)
    {
        return
            doPostAsync(
                "resolve-agent-action",
                ResolveAgentActionReply.class,
                request);
    }

    public AgentCommandServiceClient
    setAuthorizationHeader(String accessToken)
    {
        setHeader("Authorization","Bearer " + accessToken);
        return this;
    }

    private static String
    preprocessBaseUrl(String baseUrl)
    {
        return
            baseUrl.endsWith("/")
                ? baseUrl
                : baseUrl + '/';
    }
}

//////////////////////////////////////////////////////////////////////////////
