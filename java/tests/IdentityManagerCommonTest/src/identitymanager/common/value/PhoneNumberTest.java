package identitymanager.common.value;

import static org.junit.Assert.*;
import org.junit.Test;

public 
class PhoneNumberTest
{

    @Test
    public void 
    testCopy()
    {
        PhoneNumber original = new PhoneNumber( "1","562","5551234" );
        PhoneNumber copy     = original.copy();
        
        assertEquals( copy,original );
    }

    @Test
    public void 
    testCompareTo()
    {
        PhoneNumber phone1 = new PhoneNumber( "1","562","5551234" );
        PhoneNumber phone2 = phone1.copy();
        PhoneNumber phone3 = new PhoneNumber( "1","714","5551234" );
        
        assertTrue( phone1.compareTo( phone1 ) == 0 );
        assertTrue( phone1.compareTo( phone2 ) == 0 );
        assertTrue( phone2.compareTo( phone1 ) == 0 );
        assertTrue( phone1.compareTo( phone3 ) < 0 );
        assertTrue( phone3.compareTo( phone1 ) > 0 );
    }

    @Test
    public void 
    testEqualsObject()
    {
        PhoneNumber phone1 = new PhoneNumber( "1","562","5551234" );
        PhoneNumber phone2 = phone1.copy();
        PhoneNumber phone3 = new PhoneNumber( "1","714","5551234" );
        
        assertTrue( phone1.equals( phone1 ) );
        assertTrue( phone1.equals( phone2 ) );
        assertTrue( phone2.equals( phone1 ) );
        assertFalse( phone1.equals( phone3 ) );
        assertFalse( phone3.equals( phone1 ) );
    }

    @Test
    public void 
    testToString()
    {
        PhoneNumber phone = new PhoneNumber( "1","562","5551234" );
        
        assertEquals( "1-562-5551234",phone.toString() );
    }

}

// ##########################################################################
