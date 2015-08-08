package identitymanager.common.value;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public 
class PersonNameTest
{

    @Before
    public void 
    setUp() throws Exception
    {
    }

    @After
    public void 
    tearDown() throws Exception
    {
    }

    @Test
    public void 
    testCopy()
    {
        PersonName original = new PersonName("Charles","Baker","Abel");
        PersonName copy     = original.copy();
        
        assertEquals( copy,original );
    }

    @Test
    public void 
    testCompareTo()
    {
        PersonName name1 = new PersonName("Abel","Abel","Abel");
        PersonName name2 = name1.copy();
        PersonName name3 = new PersonName("Abel","Abel","Baker");
        PersonName name4 = new PersonName("Abel","Baker","Abel");
        PersonName name5 = new PersonName("Baker","Abel","Abel");
        
        assertTrue( name1.compareTo( name1 ) == 0 );
        assertTrue( name1.compareTo( name2 ) == 0 );
        assertTrue( name1.compareTo( name3 ) < 0 );
        assertTrue( name1.compareTo( name4 ) < 0 );
        assertTrue( name1.compareTo( name5 ) < 0 );
        assertTrue( name3.compareTo( name1 ) > 0 );
        assertTrue( name4.compareTo( name1 ) > 0 );
        assertTrue( name5.compareTo( name1 ) > 0 );
    }

    @Test
    public void 
    testEqualsObject()
    {
        PersonName name1 = new PersonName("Charles","Baker","Abel");
        PersonName name2 = name1.copy();
        PersonName name3 = new PersonName("Abel","Baker","Charles");
        
        assertTrue( name1.equals( name1 ) );
        assertTrue( name1.equals( name2 ) );
        assertTrue( name2.equals( name1 ) );
        assertFalse( name1.equals( name3 ) );
        assertFalse( name3.equals( name1 ) );
    }

    @Test
    public void 
    testToString()
    {
        PersonName name = new PersonName("Abel","Baker","Charles");
        
        assertEquals( "Abel Baker Charles",name.toString() );
    }

    @Test
    public void 
    testHasFirstName()
    {
        PersonName name1 = new PersonName("Abel","Abel","Abel");
        PersonName name2 = new PersonName("","Abel","Abel");
        
        assertTrue( name1.hasFirstName() );
        assertFalse( name2.hasFirstName() );
    }

    @Test
    public void 
    testHasMiddleName()
    {
        PersonName name1 = new PersonName("Abel","Abel","Abel");
        PersonName name2 = new PersonName("Abel","Abel");
        
        assertTrue( name1.hasMiddleName() );
        assertFalse( name2.hasMiddleName() );
    }

    @Test
    public void 
    testHasLastName()
    {
        PersonName name1 = new PersonName("Abel","Abel");
        PersonName name2 = new PersonName("Abel","Abel","");
        
        assertTrue( name1.hasLastName() );
        assertFalse( name2.hasLastName() );
    }

}

// ##########################################################################
