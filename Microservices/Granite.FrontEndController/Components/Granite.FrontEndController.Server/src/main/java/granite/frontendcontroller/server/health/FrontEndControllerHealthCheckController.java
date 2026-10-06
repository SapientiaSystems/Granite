//////////////////////////////////////////////////////////////////////////////
// FrontEndControllerHealthCheckController.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("${strata.health.base-path:/health}")
public
class FrontEndControllerHealthCheckController
{
    private final Logger logger;
    private Instant startupTime;
    public FrontEndControllerHealthCheckController()
    {
        this.logger = LogManager.getLogger(FrontEndControllerHealthCheckController.class);
        this.startupTime = Instant.now();
    }

    @GetMapping(
        value    = "/liveness",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String,Object>>>
    liveness()
    {
        logger.info("checking liveness");
        return
            Mono.just(
                ResponseEntity
                    .ok(
                        Map.of(
                            "status","UP",
                            "check","liveness",
                            "timestamp",Instant.now().toString())));
    }


    @GetMapping(
        value    = "/readiness",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String,Object>>>
    readiness()
    {
        logger.info("checking readiness");
        return
            Mono.just(
                ResponseEntity
                    .ok(
                        Map.of(
                            "status","UP",
                            "check","readiness",
                            "timestamp",Instant.now().toString())));
    }

    @GetMapping(
        value    = "/startup",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String,Object>>>
    startup()
    {
        boolean started = true;

        Map<String,Object> body =
            Map.of(
                "status",started ? "UP" : "DOWN",
                "check","startup",
                "startupTime",startupTime.toString(),
                "timestamp",Instant.now().toString());

        logger.info("checking startup");
        return
            Mono.just(
                started
                    ? ResponseEntity.ok(body)
                    : ResponseEntity
                    .status(503)
                    .body(body));
    }

}

//////////////////////////////////////////////////////////////////////////////
