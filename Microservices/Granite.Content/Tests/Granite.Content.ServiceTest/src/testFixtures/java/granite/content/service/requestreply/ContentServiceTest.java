//////////////////////////////////////////////////////////////////////////////
// ContentServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag("IntegrationStage")
public abstract
class ContentServiceTest
{
    @Test
    public void
    testUpsertCreatesThenUpdates()
    {
        String key = unique("guide");

        ContentDocumentData created = upsert(document(key,"Ordering Guide","How to place an order with the ordering screen.","1")).get(0);

        Assertions.assertNotNull(created.getDocumentId());
        Assertions.assertEquals(key,created.getKey());
        Assertions.assertEquals(ContentKind.GUIDE,created.getKind());
        Assertions.assertEquals(ContentFormat.MARKDOWN,created.getFormat());
        Assertions.assertNotNull(created.getUpdated());

        ContentDocumentData updated = upsert(document(key,"Ordering Guide v2","Updated body.","2")).get(0);

        Assertions.assertEquals(created.getDocumentId(),updated.getDocumentId());
        Assertions.assertEquals("Ordering Guide v2",updated.getTitle());
        Assertions.assertEquals("2",updated.getVersion());
    }

    @Test
    public void
    testUpsertRejectsInvalidKey()
    {
        UpsertContentDocumentsReply reply =
            getCommandSubject()
                .upsertContentDocuments(
                    new UpsertContentDocumentsRequest().setDocuments(List.of(document("Bad Key!","Title","Body","1"))))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(reply.isSuccess());
        Assertions.assertEquals(Set.of("Bad Key!"),reply.getFailedKeys());
    }

    @Test
    public void
    testSearchRanksTitleMatchesFirstAndFiltersByAuthority()
    {
        String open   = unique("zebra-open");
        String hidden = unique("zebra-admin");

        upsert(
            document(open,"Zebra Crossing Rules","Pedestrians cross where the stripes are.","1"),
            document(hidden,"Admin Notes","The zebra procedure for administrators only.","1").setRequiredAuthorities(Set.of("ROLE_ADMIN")));

        List<ContentHitData> asUser  = search("zebra",Set.of("ROLE_USER"));
        List<ContentHitData> asAdmin = search("zebra",Set.of("ROLE_ADMIN"));

        Assertions.assertTrue(asUser.stream().anyMatch(h -> h.getKey().equals(open)));
        Assertions.assertTrue(asUser.stream().noneMatch(h -> h.getKey().equals(hidden)));
        Assertions.assertTrue(asAdmin.stream().anyMatch(h -> h.getKey().equals(hidden)));
        Assertions.assertEquals(open,asAdmin.get(0).getKey());
        Assertions.assertTrue(asAdmin.get(0).getSnippet().toLowerCase().contains("stripes") || asAdmin.get(0).getSnippet().toLowerCase().contains("pedestrians"));
    }

    @Test
    public void
    testSearchRejectsBlankQuery()
    {
        SearchContentReply reply =
            getQuerySubject()
                .searchContent(new SearchContentRequest().setQuery("  "))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(reply.isSuccess());
    }

    @Test
    public void
    testFindByKeysAndDestroy()
    {
        String key = unique("ref");

        upsert(document(key,"Reference","Reference body.","1").setKind(ContentKind.REFERENCE));

        FindContentDocumentsByKeysReply found =
            getQuerySubject()
                .findContentDocumentsByKeys(new FindContentDocumentsByKeysRequest().setKeys(Set.of(key)))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(found.isSuccess(),found.getFailureMessage());
        Assertions.assertEquals("Reference body.",found.getDocuments().get(0).getBody());

        DestroyContentDocumentsReply destroyed =
            getCommandSubject()
                .destroyContentDocuments(new DestroyContentDocumentsRequest().setKeys(Set.of(key,"missing/key")))
                .toCompletableFuture()
                .join();

        Assertions.assertFalse(destroyed.isSuccess());
        Assertions.assertEquals(Set.of(key),destroyed.getDestroyedKeys());
        Assertions.assertEquals(Set.of("missing/key"),destroyed.getFailedKeys());
    }

    @Test
    public void
    testPackagedGraniteHelpIsAvailable()
    {
        FindAllContentDocumentsReply all =
            getQuerySubject()
                .findAllContentDocuments(new FindAllContentDocumentsRequest().setKinds(Set.of(ContentKind.HELP)))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(all.isSuccess(),all.getFailureMessage());
        Assertions.assertTrue(all.getDocuments().stream().anyMatch(d -> d.getKey().equals("granite/getting-started")));
        Assertions.assertTrue(all.getDocuments().stream().allMatch(d -> d.getKind() == ContentKind.HELP));
    }

    protected abstract IContentCommandService
    getCommandSubject();

    protected abstract IContentQueryService
    getQuerySubject();

    protected List<ContentDocumentData>
    upsert(UpsertContentDocumentData... documents)
    {
        UpsertContentDocumentsReply reply =
            getCommandSubject()
                .upsertContentDocuments(new UpsertContentDocumentsRequest().setDocuments(List.of(documents)))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        return reply.getUpsertedDocuments();
    }

    protected List<ContentHitData>
    search(String query,Set<String> authorities)
    {
        SearchContentReply reply =
            getQuerySubject()
                .searchContent(new SearchContentRequest().setQuery(query).setAuthorities(authorities).setLimit(20))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        return reply.getHits();
    }

    protected UpsertContentDocumentData
    document(String key,String title,String body,String version)
    {
        return
            new UpsertContentDocumentData()
                .setKey(key)
                .setTitle(title)
                .setKind(ContentKind.GUIDE)
                .setFormat(ContentFormat.MARKDOWN)
                .setBody(body)
                .setSource("test")
                .setVersion(version);
    }

    private String
    unique(String prefix)
    {
        return "test/" + prefix + "-" + UUID.randomUUID().toString().substring(0,8);
    }
}

//////////////////////////////////////////////////////////////////////////////
