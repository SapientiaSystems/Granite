//////////////////////////////////////////////////////////////////////////////
// AgentTurnRunner.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import granite.agent.server.domain.Conversation;
import granite.agent.service.requestreply.AgentMessageRole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public
class AgentTurnRunner
{
    public static final int    DEFAULT_MAX_STEPS = 8;
    public static final String DECLINED          = "The user declined this action.";

    private static final ObjectMapper                        OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<AgentToolCall>>  CALLS         = new TypeReference<>() {};
    private static final TypeReference<List<AgentToolOutcome>> OUTCOMES    = new TypeReference<>() {};

    private final IAgentProvider   provider;
    private final List<IAgentTool> tools;
    private final String           systemPrompt;
    private final Clock            clock;
    private final int              maxSteps;
    private final Logger           logger;

    public
    AgentTurnRunner(IAgentProvider provider,List<IAgentTool> tools,String systemPrompt,Clock clock,int maxSteps)
    {
        this.provider     = Objects.requireNonNull(provider,"provider");
        this.tools        = List.copyOf(tools == null ? List.of() : tools);
        this.systemPrompt = systemPrompt == null ? AgentSystemPrompt.GRANITE : systemPrompt;
        this.clock        = Objects.requireNonNull(clock,"clock");
        this.maxSteps     = maxSteps > 0 ? maxSteps : DEFAULT_MAX_STEPS;
        this.logger       = LogManager.getLogger(AgentTurnRunner.class);
    }

    public Conversation
    send(Conversation conversation,String text,AgentToolContext context)
    {
        List<AgentToolOutcome> unsent = read(conversation.getCollectedResults(),OUTCOMES);

        conversation.setCollectedResults(null).append(AgentMessageRole.USER,text,null,false,now());
        return loop(conversation,new AgentTurnRequest().setUserText(text).setToolOutcomes(unsent),context);
    }

    public Conversation
    resolve(Conversation conversation,boolean approved,AgentToolContext context)
    {
        List<AgentToolCall>    pending   = read(conversation.getPendingCalls(),CALLS);
        List<AgentToolOutcome> collected = read(conversation.getCollectedResults(),OUTCOMES);

        if (pending.isEmpty())
            return conversation;

        AgentToolCall first = pending.get(0);
        IAgentTool    tool  = visible(first.getName(),context);

        conversation.setPendingCalls(null).setPendingSummary(null).setCollectedResults(null);

        if (approved && tool != null)
            collected.add(execute(conversation,tool,first,context));
        else
        {
            conversation.append(AgentMessageRole.TOOL,DECLINED,first.getName(),true,now());
            collected.add(new AgentToolOutcome().setToolUseId(first.getId()).setToolName(first.getName()).setContent(DECLINED).setError(true));
        }

        if (!process(conversation,pending.subList(1,pending.size()),collected,context))
            return conversation;

        return loop(conversation,new AgentTurnRequest().setToolOutcomes(collected),context);
    }

    public List<IAgentTool>
    visibleTools(AgentToolContext context)
    {
        List<IAgentTool> result = new ArrayList<>();

        for (IAgentTool tool : tools)
            if (allowed(tool,context))
                result.add(tool);

        return result;
    }

    protected Conversation
    loop(Conversation conversation,AgentTurnRequest request,AgentToolContext context)
    {
        AgentTurnRequest next = request;

        for (int step = 0; step < maxSteps; step++)
        {
            AgentTurnStep result =
                provider.step(
                    next
                        .setHistory(conversation.getHistory())
                        .setTools(visibleTools(context))
                        .setSystemPrompt(systemPrompt));

            conversation
                .setHistory(result.getHistory())
                .addUsage(result.getInputTokens(),result.getOutputTokens());

            if (result.getText() != null && !result.getText().isBlank())
                conversation.append(AgentMessageRole.ASSISTANT,result.getText(),null,false,now());

            if (result.getToolCalls().isEmpty())
            {
                if ("refusal".equals(result.getStopReason()) && (result.getText() == null || result.getText().isBlank()))
                    conversation.append(AgentMessageRole.ASSISTANT,"I can't help with that request.",null,false,now());

                if ("max_tokens".equals(result.getStopReason()))
                    conversation.append(AgentMessageRole.ASSISTANT,"(The answer was cut short.)",null,true,now());

                return conversation;
            }

            List<AgentToolOutcome> outcomes = new ArrayList<>();

            if (!process(conversation,result.getToolCalls(),outcomes,context))
                return conversation;

            if (step == maxSteps - 1)
            {
                conversation
                    .setCollectedResults(write(outcomes))
                    .append(AgentMessageRole.ASSISTANT,"I stopped after " + maxSteps + " steps. Ask me to continue if you need more.",null,true,now());
                return conversation;
            }

            next = new AgentTurnRequest().setToolOutcomes(outcomes);
        }

        return conversation;
    }

    protected boolean
    process(Conversation conversation,List<AgentToolCall> calls,List<AgentToolOutcome> outcomes,AgentToolContext context)
    {
        for (int i = 0; i < calls.size(); i++)
        {
            AgentToolCall call = calls.get(i);
            IAgentTool    tool = visible(call.getName(),context);

            if (tool == null)
            {
                outcomes.add(new AgentToolOutcome().setToolUseId(call.getId()).setToolName(call.getName()).setContent("unknown or unavailable tool").setError(true));
                continue;
            }

            if (tool.isMutating())
            {
                String summary = tool.summarize(call.getInput());

                conversation
                    .setPendingCalls(write(new ArrayList<>(calls.subList(i,calls.size()))))
                    .setCollectedResults(write(outcomes))
                    .setPendingSummary(summary)
                    .append(AgentMessageRole.TOOL,"Waiting for your approval: " + summary,tool.getName(),false,now());
                return false;
            }

            outcomes.add(execute(conversation,tool,call,context));
        }

        return true;
    }

    protected AgentToolOutcome
    execute(Conversation conversation,IAgentTool tool,AgentToolCall call,AgentToolContext context)
    {
        AgentToolResult result;

        try
        {
            result = tool.execute(call.getInput(),context);
        }
        catch (RuntimeException e)
        {
            logger.warn("tool {} failed: {}",tool.getName(),e.getClass().getSimpleName());
            result = AgentToolResult.error("the tool failed");
        }

        conversation.append(AgentMessageRole.TOOL,tool.summarize(call.getInput()),tool.getName(),result.isError(),now());
        return new AgentToolOutcome().setToolUseId(call.getId()).setToolName(tool.getName()).setContent(result.getContent()).setError(result.isError());
    }

    private IAgentTool
    visible(String name,AgentToolContext context)
    {
        for (IAgentTool tool : tools)
            if (tool.getName().equals(name) && allowed(tool,context))
                return tool;

        return null;
    }

    private boolean
    allowed(IAgentTool tool,AgentToolContext context)
    {
        if (tool.getRequiredAuthorities().isEmpty())
            return true;

        for (String authority : tool.getRequiredAuthorities())
            if (context.getAuthorities().contains(authority))
                return true;

        return false;
    }

    private Instant
    now() { return Instant.now(clock); }

    private static <T> List<T>
    read(String json,TypeReference<List<T>> type)
    {
        if (json == null || json.isBlank())
            return new ArrayList<>();

        try
        {
            return new ArrayList<>(OBJECT_MAPPER.readValue(json,type));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to read pending agent state",e);
        }
    }

    private static String
    write(Object value)
    {
        try
        {
            return OBJECT_MAPPER.writeValueAsString(value);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to write pending agent state",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
