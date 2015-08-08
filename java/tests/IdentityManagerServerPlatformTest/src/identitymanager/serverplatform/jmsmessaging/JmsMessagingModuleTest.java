package identitymanager.serverplatform.jmsmessaging;

import identitymanager.integration.messaging.MessagingModule;
import identitymanager.integration.messaging.MessagingModuleTest;

public 
class JmsMessagingModuleTest
    extends MessagingModuleTest
{

    public 
    JmsMessagingModuleTest() {}

    @Override
    protected MessagingModule 
    createTarget()
    {
        return new JmsMessagingModule();
    }

}

// ##########################################################################
