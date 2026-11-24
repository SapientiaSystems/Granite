//////////////////////////////////////////////////////////////////////////////
// ClientQueryServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.platform;

import granite.client.service.requestreply.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletionStage;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/client-query-service")
@EnableTransactionManagement
public
class ClientQueryServiceController
{
    private final IClientQueryService implementation;
    private final Logger              logger;

    @Inject
    public
    ClientQueryServiceController(IClientQueryService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(ClientQueryServiceController.class);
    }

    @PostMapping(value = "/find-all-clients",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindClientsReply>
    findAllClients(@RequestBody FindAllClientsRequest request)
    {
        logger.info("delegating findAllClients to implementation service");
        return implementation.findAllClients(request);
    }

    @PostMapping(value = "/find-clients-by-ids",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindClientsReply>
    findClientsByIds(@RequestBody FindClientsByIdsRequest request)
    {
        logger.info("delegating findClientsByIds to implementation service");
        return implementation.findClientsByIds(request);
    }

    @PostMapping(value = "/find-clients-by-owner",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindClientsReply>
    findClientsByOwner(@RequestBody FindClientsByOwnerRequest request)
    {
        logger.info("delegating findClientsByOwner to implementation service");
        return implementation.findClientsByOwner(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
