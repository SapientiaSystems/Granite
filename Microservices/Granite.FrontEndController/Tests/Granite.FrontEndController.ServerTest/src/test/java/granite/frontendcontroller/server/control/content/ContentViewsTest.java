//////////////////////////////////////////////////////////////////////////////
// ContentViewsTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.content;

import granite.content.client.requestreply.IContentQueryServiceClient;
import granite.content.service.requestreply.ContentDocumentData;
import granite.content.service.requestreply.ContentHitData;
import granite.content.service.requestreply.ContentKind;
import granite.content.service.requestreply.FindAllContentDocumentsReply;
import granite.content.service.requestreply.FindAllContentDocumentsRequest;
import granite.content.service.requestreply.FindContentDocumentsByKeysReply;
import granite.content.service.requestreply.FindContentDocumentsByKeysRequest;
import granite.content.service.requestreply.SearchContentReply;
import granite.content.service.requestreply.SearchContentRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ContentInput;
import granite.frontendcontroller.service.requestreply.ContentOutput;
import granite.frontendcontroller.service.requestreply.GetContentOutputRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ContentViewsTest
{
    private IContentQueryServiceClient queryClient;
    private GraniteHelpServiceClients  clients;

    @BeforeEach
    public void
    setUp()
    {
        queryClient = Mockito.mock(IContentQueryServiceClient.class);
        clients     = new GraniteHelpServiceClients().setContentQueryClient(() -> queryClient);

        Mockito
            .when(queryClient.findAllContentDocuments(Mockito.any(FindAllContentDocumentsRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllContentDocumentsReply()
                        .setSuccess(true)
                        .setDocuments(List.of(new ContentDocumentData().setKey("granite/users-help").setTitle("Working with Users").setKind(ContentKind.HELP).setBody("long body")))));
        Mockito
            .when(queryClient.searchContent(Mockito.any(SearchContentRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new SearchContentReply().setSuccess(true).setHits(List.of(new ContentHitData().setKey("granite/users-help").setTitle("Working with Users").setScore(5.0)))));
        Mockito
            .when(queryClient.findContentDocumentsByKeys(Mockito.any(FindContentDocumentsByKeysRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindContentDocumentsByKeysReply().setSuccess(true).setDocuments(List.of(new ContentDocumentData().setKey("granite/users-help").setBody("Body")))));
    }

    @Test
    public void
    indexListsTitlesWithoutBodiesForTheCallersAuthorities()
        throws Exception
    {
        ContentOutput output =
            new InitializeContentView(clients,context(7L,List.of("ROLE_USER","SCOPE_internal")))
                .apply((GetContentOutputRequest)new GetContentOutputRequest().setSource("Content.All").setDestination("ContentView"))
                .toCompletableFuture()
                .get();

        Mockito.verify(queryClient).findAllContentDocuments(Mockito.argThat(r -> r.getAuthorities().equals(Set.of("ROLE_USER"))));
        assertEquals(1,output.getIndex().size());
        assertEquals("Working with Users",output.getIndex().get(0).getTitle());
        assertNull(output.getIndex().get(0).getSnippet());
        assertEquals(7L,output.getUserId());
    }

    @Test
    public void
    searchPassesTrimmedQueryAndAuthorities()
        throws Exception
    {
        ContentOutput output = run(new SearchContentView(clients,context(7L,List.of("ROLE_ADMIN"))),input().setQuery("  users  "));

        Mockito.verify(queryClient).searchContent(
            Mockito.argThat(r -> r.getQuery().equals("users") && r.getAuthorities().equals(Set.of("ROLE_ADMIN")) && r.getLimit() == SearchContentView.LIMIT));
        assertEquals(1,output.getHits().size());
        assertTrue(output.getFailureMessages().isEmpty());
    }

    @Test
    public void
    searchRequiresAQuery()
        throws Exception
    {
        ContentOutput output = run(new SearchContentView(clients,context(7L,List.of())),input().setQuery(" "));

        Mockito.verifyNoInteractions(queryClient);
        assertEquals(List.of("query is required"),output.getFailureMessages());
    }

    @Test
    public void
    openReturnsTheArticleOrNotFound()
        throws Exception
    {
        ContentOutput found = run(new OpenContentView(clients,context(7L,List.of())),input().setArticleKey("granite/users-help"));

        assertEquals("Body",found.getArticle().getBody());

        Mockito
            .when(queryClient.findContentDocumentsByKeys(Mockito.any(FindContentDocumentsByKeysRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindContentDocumentsByKeysReply().setSuccess(true).setDocuments(List.of())));

        ContentOutput missing = run(new OpenContentView(clients,context(7L,List.of())),input().setArticleKey("granite/admin-only"));

        assertNull(missing.getArticle());
        assertEquals(List.of("granite/admin-only: not found"),missing.getFailureMessages());
    }

    @Test
    public void
    refusesWithoutVerifiedCaller()
        throws Exception
    {
        ContentOutput output = run(new SearchContentView(clients,new SessionRequestContext(Map.of(),null)),input().setQuery("users"));

        Mockito.verifyNoInteractions(queryClient);
        assertEquals(List.of(UnauthenticatedContentOutputs.MESSAGE),output.getFailureMessages());
    }

    private ContentInput
    input()
    {
        return
            (ContentInput)new ContentInput()
                .setSource("Content.All")
                .setCommand("Search")
                .setDestination("ContentView");
    }

    private ContentOutput
    run(IInputAction<ContentInput,ContentOutput> action,ContentInput input)
        throws Exception
    {
        return action.apply(input).toCompletableFuture().get();
    }

    private SessionRequestContext
    context(Long caller,List<String> authorities)
    {
        Jwt jwt =
            Jwt.withTokenValue("verified.token.value")
                .header("alg","RS256")
                .jti("11")
                .subject(String.valueOf(caller))
                .claim("authorities",authorities)
                .build();

        return new SessionRequestContext(Map.of(),jwt);
    }
}

//////////////////////////////////////////////////////////////////////////////
