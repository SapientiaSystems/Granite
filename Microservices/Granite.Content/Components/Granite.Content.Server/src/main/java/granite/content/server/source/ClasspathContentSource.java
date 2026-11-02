//////////////////////////////////////////////////////////////////////////////
// ClasspathContentSource.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.source;

import granite.content.service.requestreply.ContentFormat;
import granite.content.service.requestreply.ContentKind;
import granite.content.service.requestreply.UpsertContentDocumentData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public
class ClasspathContentSource
    implements IContentSource
{
    public static final String DEFAULT_PATTERN = "classpath*:content/**/*.md";

    private final String name;
    private final String pattern;
    private final Logger logger;

    public
    ClasspathContentSource()
    {
        this("classpath",DEFAULT_PATTERN);
    }

    public
    ClasspathContentSource(String name,String pattern)
    {
        this.name    = name;
        this.pattern = pattern;
        this.logger  = LogManager.getLogger(ClasspathContentSource.class);
    }

    @Override
    public String
    getName() { return name; }

    @Override
    public List<UpsertContentDocumentData>
    load()
    {
        List<UpsertContentDocumentData> documents = new ArrayList<>();

        try
        {
            for (Resource resource : new PathMatchingResourcePatternResolver().getResources(pattern))
            {
                try (InputStream in = resource.getInputStream())
                {
                    documents.add(parse(resource.getFilename(),new String(in.readAllBytes(),StandardCharsets.UTF_8)));
                }
                catch (RuntimeException e)
                {
                    logger.warn("Skipping content resource {}: {}",resource.getFilename(),e.getMessage());
                }
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to read content resources " + pattern,e);
        }

        return documents;
    }

    public static UpsertContentDocumentData
    parse(String fileName,String text)
    {
        String              normalized = text.replace("\r\n","\n");
        Map<String,String>  front      = new LinkedHashMap<>();
        String              body       = normalized;

        if (normalized.startsWith("---\n"))
        {
            int end = normalized.indexOf("\n---\n",4);

            if (end < 0)
                throw new IllegalArgumentException("unterminated front matter");

            for (String line : normalized.substring(4,end).split("\n"))
            {
                int colon = line.indexOf(':');

                if (colon > 0)
                    front.put(line.substring(0,colon).trim().toLowerCase(Locale.ROOT),line.substring(colon + 1).trim());
            }

            body = normalized.substring(end + 5);
        }

        String key = front.getOrDefault("key",fileName == null ? null : fileName.replaceFirst("\\.md$",""));

        return
            new UpsertContentDocumentData()
                .setKey(key)
                .setTitle(front.getOrDefault("title",key))
                .setKind(front.containsKey("kind") ? ContentKind.valueOf(front.get("kind").toUpperCase(Locale.ROOT)) : ContentKind.HELP)
                .setFormat(ContentFormat.MARKDOWN)
                .setBody(body.trim())
                .setKeywords(list(front.get("keywords")))
                .setRequiredAuthorities(new TreeSet<>(list(front.get("requiredauthorities"))))
                .setVersion(front.getOrDefault("version",digest(normalized)));
    }

    private static List<String>
    list(String value)
    {
        List<String> result = new ArrayList<>();

        if (value == null || value.isBlank())
            return result;

        for (String item : Arrays.asList(value.split(",")))
            if (!item.isBlank())
                result.add(item.trim());

        return result;
    }

    private static String
    digest(String text)
    {
        try
        {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))).substring(0,32);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
