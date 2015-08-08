package identitymanager.common.value;

import static org.junit.Assert.*;
import org.junit.Test;

public class SecurityQuestionTest
{

    @Test
    public void 
    testCopy()
    {
        SecurityQuestion original = new SecurityQuestion( 1,"Mother's maiden name","Smith" );
        SecurityQuestion copy     = original.copy();
        
        assertEquals( copy,original );
    }

    @Test
    public void 
    testCompareTo()
    {
        SecurityQuestion question1 = new SecurityQuestion( 1,"Mother's maiden name","Smith" );
        SecurityQuestion question2 = question1.copy();
        SecurityQuestion question3 = new SecurityQuestion( 2,"First pet's name","Skippy" );
        
        assertTrue( question1.compareTo( question1 ) == 0 );
        assertTrue( question1.compareTo( question2 ) == 0 );
        assertTrue( question2.compareTo( question1 ) == 0 );
        assertTrue( question1.compareTo( question3 ) < 0 );
        assertTrue( question3.compareTo( question1 ) > 0 );
    }

    @Test
    public void 
    testEqualsObject()
    {
        SecurityQuestion question1 = new SecurityQuestion( 1,"Mother's maiden name","Smith" );
        SecurityQuestion question2 = question1.copy();
        SecurityQuestion question3 = new SecurityQuestion( 2,"First pet's name","Skippy" );
        
        assertTrue( question1.equals( question1 ) );
        assertTrue( question1.equals( question2 ) );
        assertTrue( question2.equals( question1 ) );
        assertFalse( question1.equals( question3 ) );
        assertFalse( question3.equals( question1 ) );
    }

    @Test
    public void 
    testToString()
    {
        SecurityQuestion question = new SecurityQuestion( 1,"Mother's maiden name","Smith" );
        
        assertEquals(
            "QuestionId: 1\n"+
            "  Question: Mother's maiden name\n" +
            "    Answer: Smith",
            question.toString() );
    }

}

// ##########################################################################
