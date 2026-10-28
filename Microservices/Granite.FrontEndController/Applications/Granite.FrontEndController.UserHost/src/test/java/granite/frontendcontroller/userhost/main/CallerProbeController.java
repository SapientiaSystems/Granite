//////////////////////////////////////////////////////////////////////////////
// CallerProbeController.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.userhost.main;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public
class CallerProbeController
{
    public static final String PATH = "/frontendcontroller-user-service/test-caller-probe";

    @PostMapping(PATH)
    public String
    caller()
    {
        return
            SessionRequestContext
                .fromCurrentRequest()
                .getCallerUserId()
                .map(String::valueOf)
                .orElse("none");
    }
}

//////////////////////////////////////////////////////////////////////////////
