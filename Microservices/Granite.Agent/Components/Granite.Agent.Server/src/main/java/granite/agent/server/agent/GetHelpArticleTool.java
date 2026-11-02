//////////////////////////////////////////////////////////////////////////////
// GetHelpArticleTool.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import granite.content.client.requestreply.IContentQueryServiceClient;
import granite.content.service.requestreply.ContentDocumentData;
import granite.content.service.requestreply.FindContentDocumentsByKeysReply;
import granite.content.service.requestreply.FindContentDocumentsByKeysRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public
class GetHelpArticleTool
    implements IAgentTool
{
    public static final String NAME       = "get_help_article";
    public static final int    MAX_LENGTH = 20000;

    private final Supplier<IContentQueryServiceClient> client;

    public
    GetHelpArticleTool(Supplier<IContentQueryServiceClient> client)
    {
        this.client = client;
    }

    @Override
    public String
    getName() { return NAME; }

    @Override
    public String
    getDescription()
    {
        return "Read the full Markdown body of one help article, user guide or reference document by its key (from search_help_content).";
    }

    @Override
    public Map<String,Object>
    getProperties()
    {
        return Map.of("key",Map.of("type","string","description","The article key, for example granite/sessions-help"));
    }

    @Override
    public List<String>
    getRequired() { return List.of("key"); }

    @Override
    public Set<String>
    getRequiredAuthorities() { return Set.of(); }

    @Override
    public boolean
    isMutating() { return false; }

    @Override
    public String
    summarize(Map<String,Object> input) { return "Read help article " + input.get("key"); }

    @Override
    public AgentToolResult
    execute(Map<String,Object> input,AgentToolContext context)
    {
        Object key = input.get("key");

        if (!(key instanceof String text) || text.isBlank())
            return AgentToolResult.error("key is required");

        FindContentDocumentsByKeysReply reply =
            client
                .get()
                .findContentDocumentsByKeys(new FindContentDocumentsByKeysRequest().setKeys(Set.of(text.trim())).setAuthorities(context.getAuthorities()))
                .toCompletableFuture()
                .join();

        if (reply == null || !reply.isSuccess())
            return AgentToolResult.error("lookup failed");

        if (reply.getDocuments().isEmpty())
            return AgentToolResult.error(text.trim() + ": not found");

        ContentDocumentData document = reply.getDocuments().get(0);
        String              body     = document.getBody() == null ? "" : document.getBody();

        return
            AgentToolResult.ok(
                "title: " + document.getTitle() + "\n\n" + (body.length() > MAX_LENGTH ? body.substring(0,MAX_LENGTH) + "\n[truncated]" : body));
    }
}

//////////////////////////////////////////////////////////////////////////////
