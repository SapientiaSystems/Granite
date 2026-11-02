//////////////////////////////////////////////////////////////////////////////
// AgentTurnRunnerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import granite.agent.server.application.MappingModule;
import granite.agent.server.domain.Conversation;
import granite.agent.service.requestreply.AgentMessageRole;
import granite.agent.service.requestreply.AgentPendingActionData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class AgentTurnRunnerTest
{
    private final List<AgentTurnRequest> requests = new ArrayList<>();
    private final List<String>           executed = new ArrayList<>();
    private final Deque<AgentTurnStep>   script   = new ArrayDeque<>();

    @Test
    public void
    answersWithoutTools()
    {
        script.add(new AgentTurnStep().setText("Hello!").setHistory("h1").setInputTokens(10).setOutputTokens(3));

        Conversation conversation = runner().send(new Conversation().setUserId(7L),"Hi",context(Set.of("ROLE_USER")));

        assertEquals(List.of(AgentMessageRole.USER,AgentMessageRole.ASSISTANT),roles(conversation));
        assertEquals("h1",conversation.getHistory());
        assertEquals(10L,conversation.getInputTokens());
        assertEquals("Hi",requests.get(0).getUserText());
        assertEquals(List.of("lookup"),names(requests.get(0).getTools()));
    }

    @Test
    public void
    runsReadToolsAndReturnsTheirResults()
    {
        script.add(new AgentTurnStep().setHistory("h1").setToolCalls(List.of(call("t1","lookup"))));
        script.add(new AgentTurnStep().setHistory("h2").setText("Found it."));

        Conversation conversation = runner().send(new Conversation().setUserId(7L),"Find",context(Set.of("ROLE_USER")));

        assertEquals(List.of("lookup"),executed);
        assertEquals("t1",requests.get(1).getToolOutcomes().get(0).getToolUseId());
        assertEquals("result of lookup",requests.get(1).getToolOutcomes().get(0).getContent());
        assertEquals(List.of(AgentMessageRole.USER,AgentMessageRole.TOOL,AgentMessageRole.ASSISTANT),roles(conversation));
        assertFalse(conversation.hasPendingAction());
    }

    @Test
    public void
    pausesOnAMutatingToolUntilApproved()
    {
        script.add(new AgentTurnStep().setHistory("h1").setToolCalls(List.of(call("t1","lookup"),call("t2","change"))));

        Conversation conversation = runner().send(new Conversation().setUserId(7L),"Change it",context(Set.of("ROLE_ADMIN")));

        assertTrue(conversation.hasPendingAction());
        assertEquals(List.of("lookup"),executed);

        AgentPendingActionData pending = MappingModule.pending(conversation);

        assertNotNull(pending);
        assertEquals("t2",pending.getToolUseId());
        assertEquals("Change the thing",pending.getSummary());

        script.add(new AgentTurnStep().setHistory("h2").setText("Done."));
        runner().resolve(conversation,true,context(Set.of("ROLE_ADMIN")));

        assertFalse(conversation.hasPendingAction());
        assertEquals(List.of("lookup","change"),executed);
        assertEquals(List.of("t1","t2"),requests.get(1).getToolOutcomes().stream().map(AgentToolOutcome::getToolUseId).toList());
    }

    @Test
    public void
    declineReturnsAnErrorResultWithoutRunningTheTool()
    {
        script.add(new AgentTurnStep().setHistory("h1").setToolCalls(List.of(call("t2","change"))));
        script.add(new AgentTurnStep().setHistory("h2").setText("Okay, I left it."));

        AgentTurnRunner runner       = runner();
        Conversation    conversation = runner.send(new Conversation().setUserId(7L),"Change it",context(Set.of("ROLE_ADMIN")));

        runner.resolve(conversation,false,context(Set.of("ROLE_ADMIN")));

        assertTrue(executed.isEmpty());

        AgentToolOutcome outcome = requests.get(1).getToolOutcomes().get(0);

        assertTrue(outcome.isError());
        assertEquals(AgentTurnRunner.DECLINED,outcome.getContent());
    }

    @Test
    public void
    toolsRequiringAuthoritiesAreHiddenAndRefused()
    {
        script.add(new AgentTurnStep().setHistory("h1").setToolCalls(List.of(call("t2","change"))));
        script.add(new AgentTurnStep().setHistory("h2").setText("I can't."));

        Conversation conversation = runner().send(new Conversation().setUserId(7L),"Change it",context(Set.of("ROLE_USER")));

        assertEquals(List.of("lookup"),names(requests.get(0).getTools()));
        assertFalse(conversation.hasPendingAction());
        assertTrue(requests.get(1).getToolOutcomes().get(0).isError());
        assertTrue(executed.isEmpty());
    }

    @Test
    public void
    stopsAfterTheStepLimitAndSendsTheResultsWithTheNextMessage()
    {
        for (int i = 0; i < 2; i++)
            script.add(new AgentTurnStep().setHistory("h" + i).setToolCalls(List.of(call("t" + i,"lookup"))));

        AgentTurnRunner runner       = new AgentTurnRunner(provider(),tools(),null,Clock.systemUTC(),2);
        Conversation    conversation = runner.send(new Conversation().setUserId(7L),"Loop",context(Set.of()));

        assertNotNull(conversation.getCollectedResults());

        script.add(new AgentTurnStep().setHistory("h9").setText("Continuing."));
        runner.send(conversation,"Continue",context(Set.of()));

        AgentTurnRequest last = requests.get(requests.size() - 1);

        assertEquals("t1",last.getToolOutcomes().get(0).getToolUseId());
        assertEquals("Continue",last.getUserText());
        assertNull(conversation.getCollectedResults());
    }

    private AgentTurnRunner
    runner()
    {
        return new AgentTurnRunner(provider(),tools(),null,Clock.systemUTC(),AgentTurnRunner.DEFAULT_MAX_STEPS);
    }

    private IAgentProvider
    provider()
    {
        return
            request ->
            {
                requests.add(
                    new AgentTurnRequest()
                        .setHistory(request.getHistory())
                        .setUserText(request.getUserText())
                        .setToolOutcomes(request.getToolOutcomes())
                        .setTools(request.getTools())
                        .setSystemPrompt(request.getSystemPrompt()));
                return script.removeFirst();
            };
    }

    private List<IAgentTool>
    tools()
    {
        return List.of(tool("lookup",false,Set.of()),tool("change",true,Set.of("ROLE_ADMIN")));
    }

    private IAgentTool
    tool(String name,boolean mutating,Set<String> authorities)
    {
        return
            new IAgentTool()
            {
                @Override
                public String getName() { return name; }

                @Override
                public String getDescription() { return name; }

                @Override
                public Map<String,Object> getProperties() { return Map.of(); }

                @Override
                public List<String> getRequired() { return List.of(); }

                @Override
                public Set<String> getRequiredAuthorities() { return authorities; }

                @Override
                public boolean isMutating() { return mutating; }

                @Override
                public String summarize(Map<String,Object> input) { return mutating ? "Change the thing" : "Look it up"; }

                @Override
                public AgentToolResult
                execute(Map<String,Object> input,AgentToolContext context)
                {
                    executed.add(name);
                    return AgentToolResult.ok("result of " + name);
                }
            };
    }

    private AgentToolCall
    call(String id,String name)
    {
        return new AgentToolCall().setId(id).setName(name).setInput(Map.of("q","x"));
    }

    private AgentToolContext
    context(Set<String> authorities)
    {
        return new AgentToolContext(7L,11L,authorities,(session,user) -> "delegated");
    }

    private static List<AgentMessageRole>
    roles(Conversation conversation)
    {
        return conversation.getTranscript().stream().map(m -> m.getRole()).toList();
    }

    private static List<String>
    names(List<IAgentTool> tools)
    {
        return tools.stream().map(IAgentTool::getName).toList();
    }
}

//////////////////////////////////////////////////////////////////////////////
