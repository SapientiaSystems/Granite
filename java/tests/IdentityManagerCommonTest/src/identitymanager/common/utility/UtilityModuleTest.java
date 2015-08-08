package identitymanager.common.utility;

import static org.junit.Assert.*;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public 
class UtilityModuleTest
{
    IContainer    itsContainer;
    UtilityModule itsTarget;
    
    @Before
    public void 
    setUp() throws Exception
    {
        itsContainer = new Container();
        itsTarget    = new UtilityModule();
    }

    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget    = null;
        itsContainer = null;
    }

    @Test
    public void 
    testInitialize()
    {
        itsTarget.initialize( itsContainer );
        
        assertNotNull( itsContainer.getInstance( IIdentifierGenerator.class ));
        assertNotNull( itsContainer.getInstance( ITimestampGenerator.class ));
    }

}

// ##########################################################################
