//////////////////////////////////////////////////////////////////////////////
// AgentViewsTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.agent;

import granite.agent.client.requestreply.IAgentCommandServiceClient;
import granite.agent.client.requestreply.IAgentQueryServiceClient;
import granite.agent.service.requestreply.ConversationData;
import granite.agent.service.requestreply.FindAgentConversationReply;
import granite.agent.service.requestreply.FindAgentConversationRequest;
import granite.agent.service.requestreply.FindAgentConversationsReply;
import granite.agent.service.requestreply.FindAgentConversationsRequest;
import granite.agent.service.requestreply.ResolveAgentActionReply;
import granite.agent.service.requestreply.ResolveAgentActionRequest;
import granite.agent.service.requestreply.SendAgentMessageReply;
import granite.agent.service.requestreply.SendAgentMessageRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.AgentInput;
import granite.frontendcontroller.service.requestreply.AgentOutput;
import granite.frontendcontroller.service.requestreply.GetAgentOutputRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("CommitStage")
public
class AgentViewsTest
{
    private IAgentCommandServiceClient commandClient;
    private IAgentQueryServiceClient   queryClient;
    private GraniteHelpServiceClients  clients;

    @BeforeEach
    public void
    setUp()
    {
        commandClient = Mockito.mock(IAgentCommandServiceClient.class);
        queryClient   = Mockito.mock(IAgentQueryServiceClient.class);
        clients       = new GraniteHelpServiceClients().setAgentCommandClient(() -> commandClient).setAgentQueryClient(() -> queryClient);

        Mockito
            .when(commandClient.sendAgentMessage(Mockito.any(SendAgentMessageRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new SendAgentMessageReply().setSuccess(true).setConversation(new ConversationData().setConversationId(3L))));
        Mockito
            .when(commandClient.resolveAgentAction(Mockito.any(ResolveAgentActionRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new ResolveAgentActionReply().setSuccess(true).setConversation(new ConversationData().setConversationId(3L))));
        Mockito
            .when(queryClient.findAgentConversations(Mockito.any(FindAgentConversationsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindAgentConversationsReply().setSuccess(true).setConversations(List.of(new ConversationData().setConversationId(3L)))));
        Mockito
            .when(queryClient.findAgentConversation(Mockito.any(FindAgentConversationRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindAgentConversationReply().setSuccess(true).setConversation(new ConversationData().setConversationId(3L).setTitle("Hi"))));
    }

    @Test
    public void
    sendStampsTheVerifiedCallerSessionAndAuthorities()
        throws Exception
    {
        AgentOutput output = new SendAgentMessageView(clients,context(7L)).apply(input("Send").setText("  Hello  ").setUserId(99L)).toCompletableFuture().get();

        Mockito.verify(commandClient).sendAgentMessage(
            Mockito.argThat(r ->
                r.getUserId().equals(7L) &&
                r.getSessionId().equals(11L) &&
                r.getText().equals("Hello") &&
                r.getAuthorities().equals(Set.of("ROLE_USER"))));
        assertEquals(3L,output.getConversation().getConversationId());
        assertEquals(7L,output.getUserId());
    }

    @Test
    public void
    approveAndDeclinePassTheDecision()
        throws Exception
    {
        new ResolveAgentActionView(clients,context(7L),true).apply(input("Approve").setConversationId(3L).setToolUseId("t1")).toCompletableFuture().get();
        new ResolveAgentActionView(clients,context(7L),false).apply(input("Decline").setConversationId(3L).setToolUseId("t1")).toCompletableFuture().get();

        Mockito.verify(commandClient).resolveAgentAction(Mockito.argThat(r -> r.getApproved() && r.getToolUseId().equals("t1") && r.getUserId().equals(7L)));
        Mockito.verify(commandClient).resolveAgentAction(Mockito.argThat(r -> !r.getApproved()));
    }

    @Test
    public void
    initializeLoadsTheLatestConversation()
        throws Exception
    {
        AgentOutput output =
            new InitializeAgentView(clients,context(7L))
                .apply((GetAgentOutputRequest)new GetAgentOutputRequest().setSource("Agent.Current").setDestination("AgentView"))
                .toCompletableFuture()
                .get();

        Mockito.verify(queryClient).findAgentConversations(Mockito.argThat(r -> r.getUserId().equals(7L)));
        assertEquals("Hi",output.getConversation().getTitle());
        assertEquals(1,output.getConversations().size());
    }

    @Test
    public void
    refusesWithoutVerifiedCaller()
        throws Exception
    {
        AgentOutput output = new SendAgentMessageView(clients,new SessionRequestContext(Map.of(),null)).apply(input("Send").setText("Hello")).toCompletableFuture().get();

        Mockito.verifyNoInteractions(commandClient,queryClient);
        assertEquals(List.of(AgentOutputs.NOT_AUTHENTICATED),output.getFailureMessages());
        assertNull(output.getConversation());
    }

    private AgentInput
    input(String command)
    {
        return (AgentInput)new AgentInput().setSource("Agent.Current").setCommand(command).setDestination("AgentView");
    }

    private SessionRequestContext
    context(Long caller)
    {
        Jwt jwt =
            Jwt.withTokenValue("verified.token.value")
                .header("alg","RS256")
                .jti("11")
                .subject(String.valueOf(caller))
                .claim("authorities",List.of("ROLE_USER","SCOPE_internal"))
                .build();

        return new SessionRequestContext(Map.of(),jwt);
    }
}

//////////////////////////////////////////////////////////////////////////////
