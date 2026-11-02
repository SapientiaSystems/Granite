//////////////////////////////////////////////////////////////////////////////
// ClaudeAgentProvider.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.core.ObjectMappers;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public
class ClaudeAgentProvider
    implements IAgentProvider
{
    public static final String DEFAULT_MODEL      = "claude-sonnet-5-5";
    public static final long   DEFAULT_MAX_TOKENS = 8000L;

    private static final TypeReference<List<MessageParam>>  HISTORY = new TypeReference<>() {};
    private static final TypeReference<Map<String,Object>>  INPUT   = new TypeReference<>() {};

    private final AnthropicClient     client;
    private final String              model;
    private final long                maxTokens;
    private final OutputConfig.Effort effort;

    public
    ClaudeAgentProvider(AnthropicClient client,String model,long maxTokens,String effort)
    {
        this.client    = Objects.requireNonNull(client,"client");
        this.model     = model == null || model.isBlank() ? DEFAULT_MODEL : model;
        this.maxTokens = maxTokens > 0 ? maxTokens : DEFAULT_MAX_TOKENS;
        this.effort    = OutputConfig.Effort.of(effort == null || effort.isBlank() ? "medium" : effort.toLowerCase());
    }

    @Override
    public AgentTurnStep
    step(AgentTurnRequest request)
    {
        List<MessageParam> messages = readHistory(request.getHistory());

        List<ContentBlockParam> user = new ArrayList<>(toolResults(request.getToolOutcomes()));

        if (request.getUserText() != null)
            user.add(ContentBlockParam.ofText(TextBlockParam.builder().text(request.getUserText()).build()));

        if (!user.isEmpty())
            messages.add(MessageParam.ofUserOfContentBlockParams(user));

        MessageCreateParams.Builder builder =
            MessageCreateParams
                .builder()
                .model(model)
                .maxTokens(maxTokens)
                .thinking(ThinkingConfigAdaptive.builder().build())
                .outputConfig(OutputConfig.builder().effort(effort).build())
                .systemOfTextBlockParams(
                    List.of(
                        TextBlockParam
                            .builder()
                            .text(request.getSystemPrompt() == null ? "" : request.getSystemPrompt())
                            .cacheControl(CacheControlEphemeral.builder().build())
                            .build()))
                .messages(messages);

        List<IAgentTool> tools = new ArrayList<>(request.getTools());

        tools.sort(Comparator.comparing(IAgentTool::getName));

        for (IAgentTool tool : tools)
            builder.addTool(toTool(tool));

        Message response = client.messages().create(builder.build());

        messages.add(response.toParam());

        StringBuilder       text  = new StringBuilder();
        List<AgentToolCall> calls = new ArrayList<>();

        for (ContentBlock block : response.content())
        {
            block.text().ifPresent(t -> text.append(text.isEmpty() ? "" : "\n\n").append(t.text()));

            if (block.isToolUse())
            {
                ToolUseBlock use = block.asToolUse();

                calls.add(new AgentToolCall().setId(use.id()).setName(use.name()).setInput(input(use._input())));
            }
        }

        return
            new AgentTurnStep()
                .setHistory(writeHistory(messages))
                .setText(text.toString())
                .setToolCalls(calls)
                .setStopReason(response.stopReason().map(Object::toString).orElse(null))
                .setInputTokens(response.usage().inputTokens())
                .setOutputTokens(response.usage().outputTokens());
    }

    public static List<MessageParam>
    readHistory(String history)
    {
        if (history == null || history.isBlank())
            return new ArrayList<>();

        try
        {
            return new ArrayList<>(ObjectMappers.jsonMapper().readValue(history,HISTORY));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to read conversation history",e);
        }
    }

    public static String
    writeHistory(List<MessageParam> messages)
    {
        try
        {
            return ObjectMappers.jsonMapper().writeValueAsString(messages);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to write conversation history",e);
        }
    }

    protected Tool
    toTool(IAgentTool tool)
    {
        Tool.InputSchema.Properties.Builder properties = Tool.InputSchema.Properties.builder();

        for (Map.Entry<String,Object> property : tool.getProperties().entrySet())
            properties.putAdditionalProperty(property.getKey(),JsonValue.from(property.getValue()));

        return
            Tool
                .builder()
                .name(tool.getName())
                .description(tool.getDescription())
                .inputSchema(
                    Tool.InputSchema
                        .builder()
                        .properties(properties.build())
                        .required(tool.getRequired())
                        .build())
                .build();
    }

    private List<ContentBlockParam>
    toolResults(List<AgentToolOutcome> outcomes)
    {
        List<ContentBlockParam> blocks = new ArrayList<>();

        for (AgentToolOutcome outcome : outcomes)
            blocks.add(
                ContentBlockParam.ofToolResult(
                    ToolResultBlockParam
                        .builder()
                        .toolUseId(outcome.getToolUseId())
                        .content(outcome.getContent() == null ? "" : outcome.getContent())
                        .isError(outcome.isError())
                        .build()));

        return blocks;
    }

    private Map<String,Object>
    input(JsonValue value)
    {
        try
        {
            Map<String,Object> converted = value.convert(INPUT);

            return converted == null ? new LinkedHashMap<>() : converted;
        }
        catch (RuntimeException e)
        {
            return new LinkedHashMap<>();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
