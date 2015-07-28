// ##########################################################################
// # File Name:	JaxbEventMapperTest.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerIntegrationTest Framework.
// #
// #   			The IdentityManagerIntegrationTest Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerIntegrationTest Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerIntegrationTest
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.integration.mapper;

import static org.junit.Assert.assertEquals;
import identitymanager.service.shared.IdentityManagerEvent;
import java.math.BigInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class JaxbEventMapperTest
{
    private IEventMapper itsTarget;
    
    /************************************************************************
     *  
     *
     * @throws java.lang.Exception
     */
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = new JaxbEventMapper();
    }

    /************************************************************************
     *  
     *
     * @throws java.lang.Exception
     */
    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget = null;
    }

    /**
     * Test method for {@link JaxbEventMapper#toPayload(IdentityManagerEvent)}.
     */
    @Test
    public void 
    testMappingCreateSubscriptionEvent() throws Exception
    {
        /*
        NotifyTrialDaysRemainingEvent expected = new NotifyTrialDaysRemainingEvent();
        NotifyTrialDaysRemainingEvent actual   = null;
        
        expected.setEventId( "1" );
        expected.setSessionId( "0" );
        expected.setDaysRemaining( 15 );
        
        actual = 
            itsTarget.toEvent( 
                NotifyTrialDaysRemainingEvent.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getEventId(),actual.getEventId() );
        assertEquals( expected.getSessionId(),actual.getSessionId() );
        assertEquals( expected.getDaysRemaining(),actual.getDaysRemaining() );
        */
    }



}

// ##########################################################################
