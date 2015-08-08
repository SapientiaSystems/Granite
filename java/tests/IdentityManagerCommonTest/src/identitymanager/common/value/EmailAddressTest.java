package identitymanager.common.value;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;

public 
class EmailAddressTest
{

    @Test
    public void 
    testCopy()
    {
        EmailAddress original = new EmailAddress("xyz@email.com");
        EmailAddress copy     = original.copy();
        
        assertEquals( copy,original );
    }

    @Test
    public void 
    testCompareTo()
    {
        EmailAddress email1 = new EmailAddress( "aaa@bbb.com" );
        EmailAddress email2 = new EmailAddress( "aaa@bbb.com" );
        EmailAddress email3 = new EmailAddress( "aab@bbb.com" );
        
        assertTrue( email1.compareTo( email2 ) == 0 );
        assertTrue( email1.compareTo( email3 ) < 0 );
        assertTrue( email3.compareTo( email2 ) > 0 );
    }

    @Test
    public void 
    testEqualsObject()
    {
        EmailAddress email1 = new EmailAddress( "aaa@bbb.com" );
        EmailAddress email2 = new EmailAddress( "aaa@bbb.com" );
        EmailAddress email3 = new EmailAddress( "aab@bbb.com" );
        
        assertTrue( email1.equals( email1 ) );
        assertTrue( email1.equals( email2 ) );
        assertFalse( email1.equals( email3 ) );
        assertFalse( email3.equals( email2 ) );
    }

    @Test
    public void 
    testToString()
    {
        EmailAddress email = new EmailAddress( "aaa@bbb.com" );
        
        assertEquals( "aaa@bbb.com",email.toString() );
    }

    @Test
    public void 
    testCreateList()
    {
        List<EmailAddress> emails = 
            EmailAddress.createList( "aaa@xyz.com,bbb@xyz.com,ccc@wxy.org" );
        
        assertEquals( 3,emails.size() );
        assertEquals( "aaa@xyz.com",emails.get( 0 ).toString() );
        assertEquals( "bbb@xyz.com",emails.get( 1 ).toString() );
        assertEquals( "ccc@wxy.org",emails.get( 2 ).toString() );
        
    }

    @Test
    public void
    testIllegalArgumentException()
    {
        try
        {
            new EmailAddress( "134##%@^" );
            fail( "EmailAddress constructor should have thrown an exception." );
        }
        catch (IllegalArgumentException e) {}
    }
}

// ##########################################################################
