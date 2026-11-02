//////////////////////////////////////////////////////////////////////////////
// ContentCommandServiceController.java
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
@RequestMapping("/content-command-service")
@EnableTransactionManagement
public
class ContentCommandServiceController
{
    private final IContentCommandService implementation;
    private final Logger                  logger;

    @Inject
    public
    ContentCommandServiceController(IContentCommandService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(ContentCommandServiceController.class);
    }

    @PostMapping(value = "/upsert-content-documents",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<UpsertContentDocumentsReply>
    upsertContentDocuments(@RequestBody UpsertContentDocumentsRequest request)
    {
        logger.info("delegating upsertContentDocuments to implementation service");
        return implementation.upsertContentDocuments(request);
    }

    @PostMapping(value = "/destroy-content-documents",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<DestroyContentDocumentsReply>
    destroyContentDocuments(@RequestBody DestroyContentDocumentsRequest request)
    {
        logger.info("delegating destroyContentDocuments to implementation service");
        return implementation.destroyContentDocuments(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
