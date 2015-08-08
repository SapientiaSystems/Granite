package identitymanager.common.utility;

import static org.junit.Assert.*;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public 
class IdentifierGeneratorTest
{
    private IIdentifierGenerator itsTarget;
    
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = new DefaultIdentifierGenerator();
    }

    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget = null;
    }

    @Test
    public void 
    testGetNextIntegerId()
    {
        Integer id = null;
        
        for (int n=0;n<100000;n++)
        {
            id = itsTarget.getNextIntegerId();
            assertNotNull( id );
            assertTrue( id >= 0 );
        }
    }

    @Test
    public void 
    testGetNextIntegerIdInt()
    {
        Integer id  = null;
        int     max = 1000;
        
        for (int n=0;n<1000000;n++)
        {
            id = itsTarget.getNextIntegerId(max);
            assertNotNull( id );
            assertTrue( id >= 0 );
            assertTrue( id < max );
        }
    }

    @Test
    public void 
    testGetNextLongId()
    {
        Long id = null;
        
        for (int n=0;n<1000000;n++)
        {
            id = itsTarget.getNextLongId();
            assertNotNull( id );
            assertTrue( id >= 0 );
        }
    }

    @Test
    public void 
    testGetNextLongIdLong()
    {
        Long id  = null;
        long max = 1000;
        
        for (int n=0;n<1000000;n++)
        {
            id = itsTarget.getNextLongId(max);
            assertNotNull( id );
            assertTrue( id >= 0 );
            assertTrue( id < max );
        }
    }

    @Test
    public void 
    testGetNextStringId()
    {
        String id = null;
        
        for (int n=0;n<10;n++)
        {
            id = itsTarget.getNextStringId();
            assertNotNull( id );
            //System.out.println( "id = " + id );
        }
        
    }

}

// ##########################################################################
