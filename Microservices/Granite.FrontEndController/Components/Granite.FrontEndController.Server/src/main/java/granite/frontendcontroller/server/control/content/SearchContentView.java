//////////////////////////////////////////////////////////////////////////////
// SearchContentView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.content;

import granite.content.service.requestreply.SearchContentReply;
import granite.content.service.requestreply.SearchContentRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ContentInput;
import granite.frontendcontroller.service.requestreply.ContentOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SearchContentView
    implements IInputAction<ContentInput,ContentOutput>
{
    public static final int MAX_QUERY_LENGTH = 500;
    public static final int LIMIT            = 20;

    private final GraniteHelpServiceClients clients;
    private final SessionRequestContext     context;

    public
    SearchContentView(GraniteHelpServiceClients clients,SessionRequestContext context)
    {
        this.clients = clients;
        this.context = context;
    }

    @Override
    public CompletionStage<ContentOutput>
    apply(ContentInput input)
    {
        Long caller = context.getCallerUserId().orElse(null);

        if (caller == null)
            return CompletableFuture.completedFuture(ContentOutputs.describe(input,UnauthenticatedContentOutputs.content(),null));

        String query = input.getQuery() == null ? "" : input.getQuery().trim();

        if (query.isEmpty() || query.length() > MAX_QUERY_LENGTH)
            return
                CompletableFuture.completedFuture(
                    ContentOutputs.describe(
                        input,
                        new ContentOutput().setFailureMessages(new ArrayList<>(List.of(query.isEmpty() ? "query is required" : "query is longer than " + MAX_QUERY_LENGTH + " characters"))),
                        caller));

        return
            clients
                .getContentQueryClient()
                .searchContent(
                    new SearchContentRequest()
                        .setQuery(query)
                        .setKinds(input.getKinds() == null ? new TreeSet<>() : new TreeSet<>(input.getKinds()))
                        .setAuthorities(context.getCallerAuthorities())
                        .setLimit(LIMIT))
                .thenApply(reply -> ContentOutputs.describe(input,toOutput(reply),caller));
    }

    private ContentOutput
    toOutput(SearchContentReply reply)
    {
        if (reply != null && reply.isSuccess())
            return new ContentOutput().setHits(reply.getHits());

        return
            new ContentOutput()
                .setFailureMessages(new ArrayList<>(List.of("Unable to search help: " + ContentOutputs.reason(reply == null ? null : reply.getFailureMessage()))));
    }
}

//////////////////////////////////////////////////////////////////////////////
