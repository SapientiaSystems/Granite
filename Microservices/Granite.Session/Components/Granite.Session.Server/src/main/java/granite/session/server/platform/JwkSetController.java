//////////////////////////////////////////////////////////////////////////////
// JwkSetController.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.application.IJwkSetProvider;
import jakarta.inject.Inject;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
public
class JwkSetController
{
    private final IJwkSetProvider provider;

    @Inject
    public
    JwkSetController(IJwkSetProvider provider)
    {
        this.provider = provider;
    }

    @GetMapping(
        value = "/.well-known/jwks.json",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>>
    getJwkSet()
    {
        return
            ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(provider.getPublicJwkSet());
    }
}

//////////////////////////////////////////////////////////////////////////////
