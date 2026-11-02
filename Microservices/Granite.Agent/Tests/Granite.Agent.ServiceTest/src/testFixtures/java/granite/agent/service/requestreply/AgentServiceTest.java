//////////////////////////////////////////////////////////////////////////////
// AgentServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

@Tag("IntegrationStage")
public abstract
class AgentServiceTest
{
    @Test
    public void
    testSendStartsAConversationForTheCaller()
    {
        ConversationData conversation = send(42L,null,"How do I end a session?");

        Assertions.assertNotNull(conversation.getConversationId());
        Assertions.assertEquals(42L,conversation.getUserId());
        Assertions.assertEquals("How do I end a session?",conversation.getTitle());
        Assertions.assertEquals(AgentMessageRole.USER,conversation.getMessages().get(0).getRole());
        Assertions.assertTrue(conversation.getMessages().stream().anyMatch(m -> m.getRole() == AgentMessageRole.ASSISTANT));
        Assertions.assertNull(conversation.getPendingAction());
    }

    @Test
    public void
    testSendRejectsBlankText()
    {
        SendAgentMessageReply reply =
            getCommandSubject()
                .sendAgentMessage(new SendAgentMessageRequest().setUserId(42L).setSessionId(11L).setText("  "))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(reply.isSuccess());
        Assertions.assertNull(reply.getConversation());
    }

    @Test
    public void
    testConversationsAreVisibleOnlyToTheirOwner()
    {
        ConversationData conversation = send(42L,null,"Hello");

        FindAgentConversationReply other =
            getQuerySubject()
                .findAgentConversation(new FindAgentConversationRequest().setUserId(43L).setConversationId(conversation.getConversationId()))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(other.isSuccess());

        SendAgentMessageReply hijack =
            getCommandSubject()
                .sendAgentMessage(
                    new SendAgentMessageRequest()
                        .setUserId(43L)
                        .setSessionId(12L)
                        .setConversationId(conversation.getConversationId())
                        .setText("Hi"))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(hijack.isSuccess());

        FindAgentConversationsReply mine =
            getQuerySubject()
                .findAgentConversations(new FindAgentConversationsRequest().setUserId(42L))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(mine.isSuccess(),mine.getFailureMessage());
        Assertions.assertTrue(mine.getConversations().stream().anyMatch(c -> c.getConversationId().equals(conversation.getConversationId())));
        Assertions.assertTrue(mine.getConversations().stream().allMatch(c -> c.getUserId().equals(42L) && c.getMessages().isEmpty()));
    }

    @Test
    public void
    testContinuesAnExistingConversation()
    {
        ConversationData first  = send(42L,null,"First question");
        ConversationData second = send(42L,first.getConversationId(),"Second question");

        Assertions.assertEquals(first.getConversationId(),second.getConversationId());
        Assertions.assertEquals("First question",second.getTitle());
        Assertions.assertTrue(second.getMessages().size() > first.getMessages().size());
    }

    protected abstract IAgentCommandService
    getCommandSubject();

    protected abstract IAgentQueryService
    getQuerySubject();

    protected ConversationData
    send(Long userId,Long conversationId,String text)
    {
        SendAgentMessageReply reply =
            getCommandSubject()
                .sendAgentMessage(
                    new SendAgentMessageRequest()
                        .setUserId(userId)
                        .setSessionId(11L)
                        .setConversationId(conversationId)
                        .setText(text)
                        .setAuthorities(Set.of("ROLE_USER")))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        return reply.getConversation();
    }
}

//////////////////////////////////////////////////////////////////////////////
