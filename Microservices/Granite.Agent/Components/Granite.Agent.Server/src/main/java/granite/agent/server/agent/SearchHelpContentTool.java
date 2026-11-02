//////////////////////////////////////////////////////////////////////////////
// SearchHelpContentTool.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import granite.content.client.requestreply.IContentQueryServiceClient;
import granite.content.service.requestreply.ContentHitData;
import granite.content.service.requestreply.SearchContentReply;
import granite.content.service.requestreply.SearchContentRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public
class SearchHelpContentTool
    implements IAgentTool
{
    public static final String NAME  = "search_help_content";
    public static final int    LIMIT = 5;

    private final Supplier<IContentQueryServiceClient> client;

    public
    SearchHelpContentTool(Supplier<IContentQueryServiceClient> client)
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
        return
            "Search the application's help articles, user guides and reference material. "
                + "Returns up to 5 hits with key, title and a snippet. Use get_help_article with a key to read the full article "
                + "before answering questions about how the application works.";
    }

    @Override
    public Map<String,Object>
    getProperties()
    {
        return Map.of("query",Map.of("type","string","description","Words to search for, for example \"end a session\""));
    }

    @Override
    public List<String>
    getRequired() { return List.of("query"); }

    @Override
    public Set<String>
    getRequiredAuthorities() { return Set.of(); }

    @Override
    public boolean
    isMutating() { return false; }

    @Override
    public String
    summarize(Map<String,Object> input) { return "Search help for \"" + input.get("query") + "\""; }

    @Override
    public AgentToolResult
    execute(Map<String,Object> input,AgentToolContext context)
    {
        Object query = input.get("query");

        if (!(query instanceof String text) || text.isBlank())
            return AgentToolResult.error("query is required");

        SearchContentReply reply =
            client
                .get()
                .searchContent(new SearchContentRequest().setQuery(text.trim()).setAuthorities(context.getAuthorities()).setLimit(LIMIT))
                .toCompletableFuture()
                .join();

        if (reply == null || !reply.isSuccess())
            return AgentToolResult.error("search failed");

        if (reply.getHits().isEmpty())
            return AgentToolResult.ok("No help articles matched.");

        StringBuilder result = new StringBuilder();

        for (ContentHitData hit : reply.getHits())
            result
                .append("key: ").append(hit.getKey())
                .append("\ntitle: ").append(hit.getTitle())
                .append("\nsnippet: ").append(hit.getSnippet() == null ? "" : hit.getSnippet())
                .append("\n\n");

        return AgentToolResult.ok(result.toString().trim());
    }
}

//////////////////////////////////////////////////////////////////////////////
