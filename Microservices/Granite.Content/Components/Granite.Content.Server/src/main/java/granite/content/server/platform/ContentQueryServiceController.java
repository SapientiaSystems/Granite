//////////////////////////////////////////////////////////////////////////////
// ContentQueryServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.platform;

import granite.content.service.requestreply.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletionStage;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/content-query-service")
@EnableTransactionManagement
public
class ContentQueryServiceController
{
    private final IContentQueryService implementation;
    private final Logger                logger;

    @Inject
    public
    ContentQueryServiceController(IContentQueryService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(ContentQueryServiceController.class);
    }

    @PostMapping(value = "/search-content",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<SearchContentReply>
    searchContent(@RequestBody SearchContentRequest request)
    {
        logger.info("delegating searchContent to implementation service");
        return implementation.searchContent(request);
    }

    @PostMapping(value = "/find-content-documents-by-keys",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindContentDocumentsByKeysReply>
    findContentDocumentsByKeys(@RequestBody FindContentDocumentsByKeysRequest request)
    {
        logger.info("delegating findContentDocumentsByKeys to implementation service");
        return implementation.findContentDocumentsByKeys(request);
    }

    @PostMapping(value = "/find-all-content-documents",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindAllContentDocumentsReply>
    findAllContentDocuments(@RequestBody FindAllContentDocumentsRequest request)
    {
        logger.info("delegating findAllContentDocuments to implementation service");
        return implementation.findAllContentDocuments(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
