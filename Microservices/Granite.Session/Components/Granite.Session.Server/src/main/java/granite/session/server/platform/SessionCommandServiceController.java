//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.service.requestreply.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletionStage;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/session-command-service")
@EnableTransactionManagement
public
class SessionCommandServiceController
{
    private final ISessionCommandService implementation;
    private final Logger                 logger;

    @Inject
    public
    SessionCommandServiceController(ISessionCommandService imp)
    {
        implementation = imp;
        logger = LogManager.getLogger(SessionCommandServiceController.class);
    }

    @PostMapping(
        value = "/login",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<LoginReply>
    login(@RequestBody LoginRequest request)
    {
        logger.info("delegating login to implementation service");
        return implementation.login(request);
    }

    @PostMapping(
        value = "/login-with-oauth2",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<LoginWithOauth2Reply>
    loginWithOauth2(@RequestBody LoginWithOauth2Request request)
    {
        logger.info("delegating loginWithOauth2 to implementation service");
        return implementation.loginWithOauth2(request);
    }

    @PostMapping(
        value = "/logout",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<LogoutReply>
    logout(@RequestBody LogoutRequest request)
    {
        logger.info("delegating logout to implementation service");
        return implementation.logout(request);
    }

    @PostMapping(
        value = "/logout-all-for-user",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<LogoutAllForUserReply>
    logoutAllForUser(@RequestBody LogoutAllForUserRequest request)
    {
        logger.info("delegating logoutAllForUser to implementation service");
        return implementation.logoutAllForUser(request);
    }

    @PostMapping(
        value = "/refresh",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<RefreshReply>
    refresh(@RequestBody RefreshRequest request)
    {
        logger.info("delegating refresh to implementation service");
        return implementation.refresh(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
