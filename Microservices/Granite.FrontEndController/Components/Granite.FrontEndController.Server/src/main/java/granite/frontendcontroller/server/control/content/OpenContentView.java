//////////////////////////////////////////////////////////////////////////////
// OpenContentView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.content;

import granite.content.service.requestreply.FindContentDocumentsByKeysReply;
import granite.content.service.requestreply.FindContentDocumentsByKeysRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ContentInput;
import granite.frontendcontroller.service.requestreply.ContentOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class OpenContentView
    implements IInputAction<ContentInput,ContentOutput>
{
    private final GraniteHelpServiceClients clients;
    private final SessionRequestContext     context;

    public
    OpenContentView(GraniteHelpServiceClients clients,SessionRequestContext context)
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

        if (input.getArticleKey() == null || input.getArticleKey().isBlank())
            return CompletableFuture.completedFuture(ContentOutputs.describe(input,failure("articleKey is required"),caller));

        String key = input.getArticleKey().trim();

        return
            clients
                .getContentQueryClient()
                .findContentDocumentsByKeys(
                    new FindContentDocumentsByKeysRequest()
                        .setKeys(Set.of(key))
                        .setAuthorities(context.getCallerAuthorities()))
                .thenApply(reply -> ContentOutputs.describe(input,toOutput(key,reply),caller));
    }

    private ContentOutput
    toOutput(String key,FindContentDocumentsByKeysReply reply)
    {
        if (reply == null || !reply.isSuccess())
            return failure("Unable to open help article: " + ContentOutputs.reason(reply == null ? null : reply.getFailureMessage()));

        if (reply.getDocuments() == null || reply.getDocuments().isEmpty())
            return failure(key + ": not found");

        return new ContentOutput().setArticle(reply.getDocuments().get(0));
    }

    private ContentOutput
    failure(String message)
    {
        return new ContentOutput().setFailureMessages(new ArrayList<>(List.of(message)));
    }
}

//////////////////////////////////////////////////////////////////////////////
