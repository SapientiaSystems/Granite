package identitymanager.common.utility;

import static org.junit.Assert.*;
import strata1.common.datetime.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import javax.xml.datatype.XMLGregorianCalendar;

public 
class TimestampGeneratorTest
{
    private ITimestampGenerator itsTarget;
    
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = new DefaultTimestampGenerator();
    }

    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget = null;
    }

    @Test
    public void 
    testGetCurrent()
    {
        DateTime             expected = null;
        XMLGregorianCalendar actual   = null;
        
        for (int n=0;n<1000;n++)
        {
            expected = new DateTime();
            actual   = itsTarget.getCurrent();
            
            assertEquals(actual.getYear(),expected.getYear());
            assertEquals(actual.getMonth(),expected.getMonth());
            assertEquals(actual.getDay(),expected.getDay());
            assertEquals(actual.getHour(),expected.getHour());
            assertEquals(actual.getSecond(),expected.getSecond());
            assertEquals(actual.getMillisecond(),expected.getMilliSecond(),50);
            assertEquals(
                actual.toGregorianCalendar().getTimeInMillis(),
                expected.getCalendar().getTimeInMillis(),
                50);
        }
    }

}

// ##########################################################################
