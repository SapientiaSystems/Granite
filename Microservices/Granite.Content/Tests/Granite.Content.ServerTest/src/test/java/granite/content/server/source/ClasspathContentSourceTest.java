//////////////////////////////////////////////////////////////////////////////
// ClasspathContentSourceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.source;

import granite.content.service.requestreply.ContentKind;
import granite.content.service.requestreply.UpsertContentDocumentData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ClasspathContentSourceTest
{
    @Test
    public void
    parsesFrontMatterAndBody()
    {
        UpsertContentDocumentData data =
            ClasspathContentSource.parse(
                "ignored.md",
                "---\r\nkey: app/orders\r\ntitle: Orders\r\nkind: guide\r\nkeywords: order, cart\r\nrequiredAuthorities: ROLE_ADMIN, ROLE_AUDITOR\r\nversion: 7\r\n---\r\n\r\n# Orders\r\n\r\nBody text.\r\n");

        assertEquals("app/orders",data.getKey());
        assertEquals("Orders",data.getTitle());
        assertEquals(ContentKind.GUIDE,data.getKind());
        assertEquals(List.of("order","cart"),data.getKeywords());
        assertEquals(Set.of("ROLE_ADMIN","ROLE_AUDITOR"),data.getRequiredAuthorities());
        assertEquals("7",data.getVersion());
        assertEquals("# Orders\n\nBody text.",data.getBody());
    }

    @Test
    public void
    defaultsKeyToFileNameAndVersionToDigest()
    {
        UpsertContentDocumentData data = ClasspathContentSource.parse("plain.md","Just text");

        assertEquals("plain",data.getKey());
        assertEquals("plain",data.getTitle());
        assertEquals(ContentKind.HELP,data.getKind());
        assertNotNull(data.getVersion());
        assertEquals(32,data.getVersion().length());
    }

    @Test
    public void
    rejectsUnterminatedFrontMatter()
    {
        assertThrows(IllegalArgumentException.class,() -> ClasspathContentSource.parse("x.md","---\nkey: a\n"));
    }

    @Test
    public void
    loadsTheGraniteHelpArticles()
    {
        List<UpsertContentDocumentData> documents = new ClasspathContentSource().load();

        assertTrue(documents.stream().anyMatch(d -> d.getKey().equals("granite/getting-started")));
        assertTrue(documents.stream().anyMatch(d -> d.getKey().equals("granite/survey-help")));
    }
}

//////////////////////////////////////////////////////////////////////////////
