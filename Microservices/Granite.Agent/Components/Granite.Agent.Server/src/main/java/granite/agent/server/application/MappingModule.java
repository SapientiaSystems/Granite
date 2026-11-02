//////////////////////////////////////////////////////////////////////////////
// MappingModule.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import granite.agent.server.agent.AgentToolCall;
import granite.agent.server.domain.Conversation;
import granite.agent.service.requestreply.AgentPendingActionData;
import granite.agent.service.requestreply.ConversationData;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Module;

import java.util.ArrayList;
import java.util.List;

public
class MappingModule
    implements Module
{
    private static final ObjectMapper                       OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<AgentToolCall>> CALLS         = new TypeReference<>() {};

    @Override
    public void
    setupModule(ModelMapper mapper)
    {
        Converter<Conversation,ConversationData> toData =
            context ->
            {
                Conversation source = context.getSource();

                return
                    source == null
                        ? null
                        : new ConversationData()
                            .setConversationId(source.getPrimaryId())
                            .setUserId(source.getUserId())
                            .setTitle(source.getTitle())
                            .setMessages(new ArrayList<>(source.getTranscript()))
                            .setPendingAction(pending(source))
                            .setInputTokens(source.getInputTokens())
                            .setOutputTokens(source.getOutputTokens())
                            .setCreated(source.getStarted())
                            .setUpdated(source.getChanged());
            };

        mapper
            .emptyTypeMap(Conversation.class,ConversationData.class)
            .setConverter(toData);
    }

    public static AgentPendingActionData
    pending(Conversation conversation)
    {
        if (!conversation.hasPendingAction())
            return null;

        try
        {
            List<AgentToolCall> calls = OBJECT_MAPPER.readValue(conversation.getPendingCalls(),CALLS);

            if (calls.isEmpty())
                return null;

            return
                new AgentPendingActionData()
                    .setToolUseId(calls.get(0).getId())
                    .setToolName(calls.get(0).getName())
                    .setSummary(conversation.getPendingSummary())
                    .setInput(OBJECT_MAPPER.writeValueAsString(calls.get(0).getInput()));
        }
        catch (Exception e)
        {
            return null;
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
