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
@RequestMapping("/user-command-service")
@EnableTransactionManagement
public
class UserCommandServiceController
{
    private IUserCommandService implementation;
    private Logger                    logger;

    @Inject
    public
    UserCommandServiceController(IUserCommandService imp)
    {
        implementation = imp;
        logger = LogManager.getLogger(UserCommandServiceController.class);
    }

    @PostMapping(
        value = "/create-user",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<CreateUserReply>
    createUser(@RequestBody CreateUserRequest request)
    {
        logger.info("delegating createUser to implementation service");
        return implementation.createUser(request);
    }

    @PostMapping(
        value = "/update-user",
        produces = MediaType.APPLICATION_JSON_VALUE)
    //@Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public @ResponseBody CompletionStage<UpdateUserReply>
    updateUser(@RequestBody UpdateUserRequest request)
    {
        logger.info("delegating updateUser to implementation service");
        return implementation.updateUser(request);
    }

    @PostMapping(
        value = "/destroy-user",
        produces = MediaType.APPLICATION_JSON_VALUE)
    //@Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public @ResponseBody CompletionStage<DestroyUserReply>
    destroyUser(@RequestBody DestroyUserRequest request)
    {
        logger.info("delegating updateUser to implementation service");
        return implementation.destroyUser(request);
    }

    @PostMapping(
        value = "/create-users",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<CreateUsersReply>
    createUsers(@RequestBody CreateUsersRequest request)
    {
        logger.info("delegating createUsers to implementation service");
        return implementation.createUsers(request);
    }

    @PostMapping(
        value = "/update-users",
        produces = MediaType.APPLICATION_JSON_VALUE)
    //@Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public @ResponseBody CompletionStage<UpdateUsersReply>
    updateUsers(@RequestBody UpdateUsersRequest request)
    {
        logger.info("delegating updateUsers to implementation service");
        return implementation.updateUsers(request);
    }

    @PostMapping(
        value = "/authenticate-user",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<AuthenticateUserReply>
    authenticateUser(@RequestBody AuthenticateUserRequest request)
    {
        logger.info("delegating authenticateUser to implementation service");
        return implementation.authenticateUser(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
