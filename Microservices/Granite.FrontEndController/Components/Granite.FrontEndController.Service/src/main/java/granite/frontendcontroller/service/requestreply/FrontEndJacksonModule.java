//////////////////////////////////////////////////////////////////////////////
// FrontEndJacksonModule.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import com.fasterxml.jackson.databind.module.SimpleModule;

public
class FrontEndJacksonModule
    extends SimpleModule
{
    public
    FrontEndJacksonModule()
    {
        super("GraniteFrontEndJacksonModule");
        registerSubtypes(UsersInput.class,UsersOutput.class);
    }
}

//////////////////////////////////////////////////////////////////////////////
