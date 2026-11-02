//////////////////////////////////////////////////////////////////////////////
// ClaudeAgentProviderTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ToolResultBlockParam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ClaudeAgentProviderTest
{
    @Test
    public void
    historyRoundTripsThroughTheSdkMapper()
    {
        List<MessageParam> messages =
            List.of(
                MessageParam.ofUser("How do I end a session?"),
                MessageParam.ofAssistant("Let me look that up."),
                MessageParam.ofUserOfContentBlockParams(
                    List.of(
                        ContentBlockParam.ofToolResult(ToolResultBlockParam.builder().toolUseId("toolu_1").content("found").isError(false).build()),
                        ContentBlockParam.ofText(TextBlockParam.builder().text("thanks").build()))));

        String             json = ClaudeAgentProvider.writeHistory(messages);
        List<MessageParam> read = ClaudeAgentProvider.readHistory(json);

        assertEquals(3,read.size());
        assertEquals(json,ClaudeAgentProvider.writeHistory(read));
        assertTrue(json.contains("toolu_1"));
    }

    @Test
    public void
    emptyHistoryIsAnEmptyList()
    {
        assertTrue(ClaudeAgentProvider.readHistory(null).isEmpty());
        assertTrue(ClaudeAgentProvider.readHistory(" ").isEmpty());
    }
}

//////////////////////////////////////////////////////////////////////////////
