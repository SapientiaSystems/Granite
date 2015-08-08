package identitymanager.common.value;

import static org.junit.Assert.*;
import org.junit.Test;

public class PostalAddressTest
{

    @Test
    public void 
    testCopy()
    {
        PostalAddress original = 
            new PostalAddress(
                "123", 
                "Main Street", 
                "Metropolis", 
                "CA", 
                "USA", 
                "90123" );
        PostalAddress copy = original.copy();
        
        assertEquals( copy,original );
    }

    @Test
    public void 
    testCompareTo()
    {
        PostalAddress post1 = 
            new PostalAddress(
                "123", 
                "Main Street", 
                "Metropolis", 
                "CA", 
                "USA", 
                "90123" );
        PostalAddress post2 = post1.copy();
        PostalAddress post3 = 
            new PostalAddress(
                "456", 
                "Main Street", 
                "Metropolis", 
                "CA", 
                "USA", 
                "90123" );
         
        assertTrue( post1.compareTo( post1 ) == 0 );
        assertTrue( post1.compareTo( post2 ) == 0 );
        assertTrue( post2.compareTo( post1 ) == 0 );
        assertTrue( post1.compareTo( post3 ) < 0 );
        assertTrue( post3.compareTo( post1 ) > 0 );      
    }

    @Test
    public void 
    testEqualsObject()
    {
        PostalAddress post1 = 
            new PostalAddress(
                "123", 
                "Main Street", 
                "Metropolis", 
                "CA", 
                "USA", 
                "90123" );
        PostalAddress post2 = post1.copy();
        PostalAddress post3 = 
            new PostalAddress(
                "456", 
                "Main Street", 
                "Metropolis", 
                "CA", 
                "USA", 
                "90123" );
        
        assertTrue( post1.equals( post1 ) );
        assertTrue( post1.equals( post2 ) );
        assertTrue( post2.equals( post1 ) );
        assertFalse( post1.equals( post3 ) );
        assertFalse( post3.equals( post1 ) );
    }

    @Test
    public void 
    testToString()
    {
        PostalAddress post1 = 
            new PostalAddress(
                "123", 
                "Main Street", 
                "Metropolis", 
                "CA", 
                "USA", 
                "90123" );
        
        assertEquals(
            "123 Main Street\n" +
            "Metropolis CA USA 90123",
            post1.toString() );
    }

}

// ##########################################################################
