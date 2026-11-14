//////////////////////////////////////////////////////////////////////////////
// ClientCommandServiceController.java
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
@RequestMapping("/client-command-service")
@EnableTransactionManagement
public
class ClientCommandServiceController
{
    private final IClientCommandService implementation;
    private final Logger                logger;

    @Inject
    public
    ClientCommandServiceController(IClientCommandService imp)
    {
        implementation = imp;
        logger         = LogManager.getLogger(ClientCommandServiceController.class);
    }

    @PostMapping(value = "/create-client",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<CreateClientReply>
    createClient(@RequestBody CreateClientRequest request)
    {
        logger.info("delegating createClient to implementation service");
        return implementation.createClient(request);
    }

    @PostMapping(value = "/update-clients",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<UpdateClientsReply>
    updateClients(@RequestBody UpdateClientsRequest request)
    {
        logger.info("delegating updateClients to implementation service");
        return implementation.updateClients(request);
    }

    @PostMapping(value = "/rotate-client-secret",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<RotateClientSecretReply>
    rotateClientSecret(@RequestBody RotateClientSecretRequest request)
    {
        logger.info("delegating rotateClientSecret to implementation service");
        return implementation.rotateClientSecret(request);
    }

    @PostMapping(value = "/rotate-owned-client-secret",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<RotateClientSecretReply>
    rotateOwnedClientSecret(@RequestBody RotateOwnedClientSecretRequest request)
    {
        logger.info("delegating rotateOwnedClientSecret to implementation service");
        return implementation.rotateOwnedClientSecret(request);
    }

    @PostMapping(value = "/rotate-own-client-secret",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<RotateOwnClientSecretReply>
    rotateOwnClientSecret(@RequestBody RotateOwnClientSecretRequest request)
    {
        logger.info("delegating rotateOwnClientSecret to implementation service");
        return implementation.rotateOwnClientSecret(request);
    }

    @PostMapping(value = "/destroy-clients",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<DestroyClientsReply>
    destroyClients(@RequestBody DestroyClientsRequest request)
    {
        logger.info("delegating destroyClients to implementation service");
        return implementation.destroyClients(request);
    }

    @PostMapping(value = "/verify-client-credentials",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<VerifyClientCredentialsReply>
    verifyClientCredentials(@RequestBody VerifyClientCredentialsRequest request)
    {
        logger.info("delegating verifyClientCredentials to implementation service");
        return implementation.verifyClientCredentials(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
