//////////////////////////////////////////////////////////////////////////////
// UserCommandServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.service.requestreply.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletionStage;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/user-query-service")
@EnableTransactionManagement
public
class UserQueryServiceController
{
    private IUserQueryService implementation;
    private Logger                  logger;

    @Inject
    public UserQueryServiceController(IUserQueryService imp)
    {
        implementation = imp;
        logger = LogManager.getLogger(UserQueryServiceController.class);
    }

    @PostMapping(
        value = "/find-users-by-key",
        produces = MediaType.APPLICATION_JSON_VALUE)
    //@Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public @ResponseBody CompletionStage<FindUsersByKeyReply>
    findUsersByKey(@RequestBody FindUsersByKeyRequest request)
    {
        logger.info("delegating findUsersByKey to implementation service");
        return implementation.findUsersByKey(request);
    }

    @PostMapping(
        value = "/find-users-by-keyset",
        produces = MediaType.APPLICATION_JSON_VALUE)
    //@Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public @ResponseBody CompletionStage<FindUsersByKeySetReply>
    findUsersByKeySet(@RequestBody FindUsersByKeySetRequest request)
    {
        logger.info("delegating findUsersByKeySet to implementation service");
        return implementation.findUsersByKeySet(request);
    }

    @PostMapping(
        value = "/find-users-by-external-identity",
        produces = MediaType.APPLICATION_JSON_VALUE)
    //@Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public @ResponseBody CompletionStage<FindUsersByExternalIdentityReply>
    findUsersByExternalIdentity(@RequestBody FindUsersByExternalIdentityRequest request)
    {
        logger.info("delegating findUsersByExternalIdentity to implementation service");
        return implementation.findUsersByExternalIdentity(request);
    }

    @PostMapping(
        value = "/find-all-users",
        produces = MediaType.APPLICATION_JSON_VALUE)
    //@Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public @ResponseBody CompletionStage<FindAllUsersReply>
    findAllUsers(@RequestBody FindAllUsersRequest request)
    {
        logger.info("delegating findAllUsers to implementation service");
        return implementation.findAllUsers(request);
    }

}

//////////////////////////////////////////////////////////////////////////////
