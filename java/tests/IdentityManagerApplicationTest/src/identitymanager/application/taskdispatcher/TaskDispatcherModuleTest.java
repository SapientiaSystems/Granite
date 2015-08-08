package identitymanager.application.taskdispatcher;

import static org.junit.Assert.*;
import strata1.common.task.ITaskConsumer;
import strata1.common.task.ITaskDispatcher;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public 
class TaskDispatcherModuleTest
{
    private IContainer           itsContainer;
    private TaskDispatcherModule itsTarget;
    
    @Before
    public void 
    setUp() throws Exception
    {
        itsContainer = new Container();
        itsTarget    = new TaskDispatcherModule();
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
        
        assertNotNull( itsContainer.getInstance( ITaskConsumer.class,"Consumer1" ) );
        assertNotNull( itsContainer.getInstance( ITaskConsumer.class,"Consumer2" ) );
        assertNotNull( itsContainer.getInstance( ITaskConsumer.class,"Consumer3" ) );
        assertNotNull( itsContainer.getInstance( ITaskConsumer.class,"Consumer4" ) );
        assertNotNull( itsContainer.getInstance( ITaskDispatcher.class ) );
    }

}

// ##########################################################################
