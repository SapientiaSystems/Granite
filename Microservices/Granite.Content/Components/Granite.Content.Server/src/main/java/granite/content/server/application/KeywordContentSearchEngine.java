//////////////////////////////////////////////////////////////////////////////
// KeywordContentSearchEngine.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.application;

import granite.content.server.domain.ContentDocument;
import granite.content.service.requestreply.ContentHitData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public
class KeywordContentSearchEngine
    implements IContentSearchEngine
{
    public static final int SNIPPET_RADIUS = 80;

    private static final double TITLE_WEIGHT   = 5.0;
    private static final double KEYWORD_WEIGHT = 3.0;
    private static final double BODY_WEIGHT    = 1.0;
    private static final int    BODY_CAP       = 5;
    private static final double PHRASE_BONUS   = 4.0;

    @Override
    public List<ContentHitData>
    search(List<ContentDocument> candidates,String query,int limit)
    {
        String                       phrase    = normalize(query);
        Set<String>                  terms     = terms(phrase);
        Map<String,ContentDocument>  documents = new LinkedHashMap<>();
        List<ContentHitData>         hits      = new ArrayList<>();

        if (terms.isEmpty())
            return hits;

        for (ContentDocument document : candidates)
        {
            double score = score(document,phrase,terms);

            if (score > 0)
            {
                documents.put(document.getDocumentKey(),document);
                hits.add(
                    new ContentHitData()
                        .setKey(document.getDocumentKey())
                        .setTitle(document.getTitle())
                        .setKind(document.getKind())
                        .setScore(score));
            }
        }

        hits.sort(
            Comparator
                .comparingDouble(ContentHitData::getScore)
                .reversed()
                .thenComparing(h -> h.getTitle() == null ? "" : h.getTitle()));

        List<ContentHitData> top = new ArrayList<>(hits.subList(0,Math.min(limit,hits.size())));

        for (ContentHitData hit : top)
            hit.setSnippet(snippet(documents.get(hit.getKey()).getBody(),terms));

        return top;
    }

    protected double
    score(ContentDocument document,String phrase,Set<String> terms)
    {
        String title    = normalize(document.getTitle());
        String body     = normalize(plainText(document.getBody()));
        String keywords = normalize(String.join(" ",document.getKeywords()));
        double score    = 0;

        for (String term : terms)
        {
            if (title.contains(term))
                score += TITLE_WEIGHT;

            if (keywords.contains(term))
                score += KEYWORD_WEIGHT;

            score += BODY_WEIGHT * Math.min(BODY_CAP,occurrences(body,term));
        }

        if (score > 0 && terms.size() > 1 && (title.contains(phrase) || body.contains(phrase)))
            score += PHRASE_BONUS;

        return score;
    }

    protected String
    snippet(String body,Set<String> terms)
    {
        String text  = plainText(body).replaceAll("\\s+"," ").trim();
        String lower = text.toLowerCase(Locale.ROOT);
        int    at    = -1;

        for (String term : terms)
        {
            int found = lower.indexOf(term);

            if (found >= 0 && (at < 0 || found < at))
                at = found;
        }

        if (at < 0)
            at = 0;

        int start = Math.max(0,at - SNIPPET_RADIUS);
        int end   = Math.min(text.length(),at + SNIPPET_RADIUS);

        return (start > 0 ? "…" : "") + text.substring(start,end).trim() + (end < text.length() ? "…" : "");
    }

    public static String
    plainText(String markdown)
    {
        if (markdown == null)
            return "";

        return
            markdown
                .replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)","$1")
                .replaceAll("(?m)^\\s{0,3}#{1,6}\\s*","")
                .replaceAll("(?m)^\\s*[-*+]\\s+","")
                .replaceAll("[*_`>]","");
    }

    private static String
    normalize(String text)
    {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).replaceAll("\\s+"," ").trim();
    }

    private static Set<String>
    terms(String phrase)
    {
        Set<String> terms = new LinkedHashSet<>();

        for (String word : phrase.split("[^\\p{L}\\p{N}]+"))
            if (word.length() >= 2)
                terms.add(word);

        return terms;
    }

    private static int
    occurrences(String text,String term)
    {
        int count = 0;
        int from  = 0;

        while ((from = text.indexOf(term,from)) >= 0)
        {
            count++;
            from += term.length();
        }

        return count;
    }
}

//////////////////////////////////////////////////////////////////////////////
