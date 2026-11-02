//////////////////////////////////////////////////////////////////////////////
// InitializeContentView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.content;

import granite.content.service.requestreply.ContentDocumentData;
import granite.content.service.requestreply.ContentHitData;
import granite.content.service.requestreply.FindAllContentDocumentsReply;
import granite.content.service.requestreply.FindAllContentDocumentsRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ContentOutput;
import granite.frontendcontroller.service.requestreply.GetContentOutputRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeContentView
    implements IOutputAction<GetContentOutputRequest,ContentOutput>
{
    private final GraniteHelpServiceClients clients;
    private final SessionRequestContext     context;

    public
    InitializeContentView(GraniteHelpServiceClients clients,SessionRequestContext context)
    {
        this.clients = clients;
        this.context = context;
    }

    @Override
    public CompletionStage<ContentOutput>
    apply(GetContentOutputRequest request)
    {
        CompletionStage<ContentOutput> output =
            context.getCallerUserId().isEmpty()
                ? CompletableFuture.completedFuture(UnauthenticatedContentOutputs.content())
                : clients
                    .getContentQueryClient()
                    .findAllContentDocuments(new FindAllContentDocumentsRequest().setAuthorities(context.getCallerAuthorities()))
                    .thenApply(this::toOutput);

        return
            output.thenApply(
                result ->
                    result
                        .setUserId(context.getCallerUserId().orElse(null))
                        .setSource(request.getSource())
                        .setCommand(request.getCommand())
                        .setDestination(request.getDestination()));
    }

    private ContentOutput
    toOutput(FindAllContentDocumentsReply reply)
    {
        if (reply == null || !reply.isSuccess())
            return
                new ContentOutput()
                    .setFailureMessages(
                        new ArrayList<>(List.of("Unable to load help content: " + ContentOutputs.reason(reply == null ? null : reply.getFailureMessage()))));

        List<ContentHitData> index = new ArrayList<>();

        for (ContentDocumentData document : reply.getDocuments())
            index.add(new ContentHitData().setKey(document.getKey()).setTitle(document.getTitle()).setKind(document.getKind()));

        return new ContentOutput().setIndex(index);
    }
}

//////////////////////////////////////////////////////////////////////////////
