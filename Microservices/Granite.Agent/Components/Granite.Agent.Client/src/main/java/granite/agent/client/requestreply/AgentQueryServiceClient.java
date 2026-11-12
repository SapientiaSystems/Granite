//////////////////////////////////////////////////////////////////////////////
// AgentQueryServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.client.requestreply;

import granite.agent.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.core.service.CountedClient;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

public
class AgentQueryServiceClient
    extends RestEasyRestClient
    implements IAgentQueryServiceClient
{
    public
    AgentQueryServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "agent-query-service/");
    }

    public
    AgentQueryServiceClient(CountedClient client,String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "agent-query-service/");
    }

    @Override
    public CompletionStage<FindAgentConversationsReply>
    findAgentConversations(FindAgentConversationsRequest request)
    {
        return
            doPostAsync(
                "find-agent-conversations",
                FindAgentConversationsReply.class,
                request);
    }

    @Override
    public CompletionStage<FindAgentConversationReply>
    findAgentConversation(FindAgentConversationRequest request)
    {
        return
            doPostAsync(
                "find-agent-conversation",
                FindAgentConversationReply.class,
                request);
    }

    public AgentQueryServiceClient
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
