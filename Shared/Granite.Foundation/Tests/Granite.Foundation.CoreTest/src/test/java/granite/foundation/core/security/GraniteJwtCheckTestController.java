//////////////////////////////////////////////////////////////////////////////
// GraniteJwtCheckTestController.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public
class GraniteJwtCheckTestController
{
    @GetMapping("/api/ping")
    public String
    ping()
    {
        return "pong";
    }

    @GetMapping("/health/live")
    public String
    live()
    {
        return "up";
    }
}

//////////////////////////////////////////////////////////////////////////////
