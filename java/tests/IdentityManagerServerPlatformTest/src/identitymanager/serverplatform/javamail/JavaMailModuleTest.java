package identitymanager.serverplatform.javamail;

import identitymanager.application.email.EmailAppModule;
import identitymanager.application.email.EmailAppModuleTest;
import identitymanager.application.email.IEmailSessionProvider;
import strata1.injector.container.IContainer;

public 
class JavaMailModuleTest
    extends EmailAppModuleTest
{

    @Override
    protected EmailAppModule createTarget()
    {
        return new JavaMailModule();
    }

}

// ##########################################################################
