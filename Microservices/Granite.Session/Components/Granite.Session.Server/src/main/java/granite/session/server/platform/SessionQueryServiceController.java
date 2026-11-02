//////////////////////////////////////////////////////////////////////////////
// SessionQueryServiceController.java
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
@RequestMapping("/session-query-service")
@EnableTransactionManagement
public
class SessionQueryServiceController
{
    private final ISessionQueryService implementation;
    private final Logger               logger;

    @Inject
    public
    SessionQueryServiceController(ISessionQueryService imp)
    {
        implementation = imp;
        logger = LogManager.getLogger(SessionQueryServiceController.class);
    }

    @PostMapping(
        value = "/find-sessions-by-key",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindSessionsByKeyReply>
    findSessionsByKey(@RequestBody FindSessionsByKeyRequest request)
    {
        logger.info("delegating findSessionsByKey to implementation service");
        return implementation.findSessionsByKey(request);
    }

    @PostMapping(
        value = "/find-sessions-by-keyset",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindSessionsByKeySetReply>
    findSessionsByKeySet(@RequestBody FindSessionsByKeySetRequest request)
    {
        logger.info("delegating findSessionsByKeySet to implementation service");
        return implementation.findSessionsByKeySet(request);
    }

    @PostMapping(
        value = "/find-sessions",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindSessionsReply>
    findSessions(@RequestBody FindSessionsRequest request)
    {
        logger.info("delegating findSessions to implementation service");
        return implementation.findSessions(request);
    }

    @PostMapping(
        value = "/find-session-by-user-id-and-device-id",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompletionStage<FindSessionByUserIdAndDeviceIdReply>
    findSessionByUserIdAndDeviceId(
        @RequestBody FindSessionByUserIdAndDeviceIdRequest request)
    {
        logger.info("delegating findSessionByUserIdAndDeviceId to implementation service");
        return implementation.findSessionByUserIdAndDeviceId(request);
    }
}

//////////////////////////////////////////////////////////////////////////////
