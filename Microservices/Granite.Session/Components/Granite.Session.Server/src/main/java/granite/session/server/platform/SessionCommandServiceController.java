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
        value = "/start-okta-login",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<StartOktaLoginReply>
    startOktaLogin(@RequestBody StartOktaLoginRequest request)
    {
        logger.info("delegating startOktaLogin to implementation service");
        return implementation.startOktaLogin(request);
    }

    @PostMapping(
        value = "/complete-okta-login",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<CompleteOktaLoginReply>
    completeOktaLogin(@RequestBody CompleteOktaLoginRequest request)
    {
        logger.info("delegating completeOktaLogin to implementation service");
        return implementation.completeOktaLogin(request);
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
        value = "/close-sessions",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<CloseSessionsReply>
    closeSessions(@RequestBody CloseSessionsRequest request)
    {
        logger.info("delegating closeSessions to implementation service");
        return implementation.closeSessions(request);
    }

    @PostMapping(
        value = "/change-password",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<ChangePasswordReply>
    changePassword(@RequestBody ChangePasswordRequest request)
    {
        logger.info("delegating changePassword to implementation service");
        return implementation.changePassword(request);
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

    @PostMapping(
        value = "/resolve-session",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<ResolveSessionReply>
    resolveSession(@RequestBody ResolveSessionRequest request)
    {
        logger.debug("delegating resolveSession to implementation service");
        return implementation.resolveSession(request);
    }

    @PostMapping(
        value = "/issue-service-token",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<IssueServiceTokenReply>
    issueServiceToken(@RequestBody IssueServiceTokenRequest request)
    {
        logger.debug("delegating issueServiceToken to implementation service");
        return implementation.issueServiceToken(request);
    }

    @PostMapping(
        value = "/issue-delegated-token",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<IssueDelegatedTokenReply>
    issueDelegatedToken(@RequestBody IssueDelegatedTokenRequest request)
    {
        logger.debug("delegating issueDelegatedToken to implementation service");
        return implementation.issueDelegatedToken(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
