//////////////////////////////////////////////////////////////////////////////
// KeywordContentSearchEngineTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.application;

import granite.content.server.domain.ContentDocument;
import granite.content.service.requestreply.ContentHitData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class KeywordContentSearchEngineTest
{
    private final KeywordContentSearchEngine engine = new KeywordContentSearchEngine();

    @Test
    public void
    titleAndKeywordMatchesOutrankBodyMatches()
    {
        List<ContentHitData> hits =
            engine.search(
                List.of(
                    document("body","Other","Sessions are mentioned here once.",List.of()),
                    document("title","Working with Sessions","Rows and devices.",List.of("device"))),
                "sessions",
                10);

        assertEquals(List.of("title","body"),hits.stream().map(ContentHitData::getKey).toList());
    }

    @Test
    public void
    snippetCentersOnTheFirstMatchWithoutMarkdown()
    {
        String body = "# Heading\n\n" + "filler ".repeat(30) + "**Kafka** relays [events](content:x) to topics.";

        ContentHitData hit = engine.search(List.of(document("k","Relay",body,List.of())),"kafka",1).get(0);

        assertTrue(hit.getSnippet().startsWith("…"));
        assertTrue(hit.getSnippet().contains("Kafka relays events to topics."));
        assertFalse(hit.getSnippet().contains("**"));
    }

    @Test
    public void
    shortOrEmptyQueriesFindNothingAndLimitIsHonoured()
    {
        List<ContentDocument> documents = List.of(document("a","Alpha","x",List.of()),document("b","Alpha two","x",List.of()));

        assertTrue(engine.search(documents,"a",10).isEmpty());
        assertEquals(1,engine.search(documents,"alpha",1).size());
    }

    @Test
    public void
    visibilityRequiresOneOfTheAuthorities()
    {
        ContentDocument document = document("k","T","b",List.of()).setRequiredAuthorities(Set.of("ROLE_ADMIN"));

        assertFalse(document.isVisibleTo(Set.of("ROLE_USER")));
        assertFalse(document.isVisibleTo(null));
        assertTrue(document.isVisibleTo(Set.of("ROLE_USER","ROLE_ADMIN")));
        assertTrue(document("open","T","b",List.of()).isVisibleTo(null));
    }

    private ContentDocument
    document(String key,String title,String body,List<String> keywords)
    {
        return new ContentDocument().setDocumentKey(key).setTitle(title).setBody(body).setKeywords(keywords);
    }
}

//////////////////////////////////////////////////////////////////////////////
