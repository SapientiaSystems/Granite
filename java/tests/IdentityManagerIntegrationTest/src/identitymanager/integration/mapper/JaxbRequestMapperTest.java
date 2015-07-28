// ##########################################################################
// # File Name:	JaxbRequestMapperTest.java
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
import identitymanager.service.authentication.UserNamePasswordAuthenticateRequest;
import identitymanager.service.shared.IdentityManagerRequest;
import identitymanager.service.useridentity.IUserIdentityEventListener;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import identitymanager.service.useridentity.StartListeningRequest;
import identitymanager.service.useridentity.StopListeningRequest;
import identitymanager.service.useridentity.UpdateUserIdentityRequest;
import identitymanager.service.useridentity.UserIdentityData;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class JaxbRequestMapperTest
{
    private IRequestMapper itsTarget;
    
    /************************************************************************
     *  
     *
     * @throws java.lang.Exception
     */
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = new JaxbRequestMapper();
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
     * Test method for {@link JaxbRequestMapper#toPayload(IdentityManagerRequest)}.
     */
    @Test
    public void 
    testMappingStartCreatingUserIdentityRequest() throws Exception
    {
        StartCreatingUserIdentityRequest expected = new StartCreatingUserIdentityRequest();
        StartCreatingUserIdentityRequest actual   = null;
        UserIdentityData                 user     = new UserIdentityData();
        
        user.setUserName( "JohnDoe" );
        user.setUserPassword( "Foobar" );
        user.setEmailAddress( "John.Doe@somewhere.com" );
        
        expected.setRequestId( "1" );
        expected.setSessionId( "0" );
        expected.setUser( user );
        
        actual = 
            itsTarget.toRequest( 
                StartCreatingUserIdentityRequest.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getRequestId(),actual.getRequestId() );
        assertEquals( expected.getSessionId(),actual.getSessionId() );
        assertEquals( 
            expected.getUser().getUserName(),
            actual.getUser().getUserName() );
        assertEquals( 
            expected.getUser().getUserPassword(),
            actual.getUser().getUserPassword() );
        assertEquals( 
            expected.getUser().getEmailAddress(),
            actual.getUser().getEmailAddress() );
    }

    /**
     * Test method for {@link JaxbRequestMapper#toPayload(IdentityManagerRequest)}.
     */
    @Test
    public void 
    testMappingUpdateUserIdentityRequest() throws Exception
    {
        UpdateUserIdentityRequest expected = new UpdateUserIdentityRequest();
        UpdateUserIdentityRequest actual   = null;
        UserIdentityData          user     = new UserIdentityData();
        
        
        user.setUserName( "JohnDoe" );
        user.setUserPassword( "Foobar" );
        user.setEmailAddress( "John.Doe@somewhere.com" );
        
        expected.setRequestId( "1" );
        expected.setSessionId( "0" );
        expected.setUser( user );
        
        actual = 
            itsTarget.toRequest( 
                UpdateUserIdentityRequest.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getRequestId(),actual.getRequestId() );
        assertEquals( expected.getSessionId(),actual.getSessionId() );
        assertEquals( 
            expected.getUser().getUserName(),
            actual.getUser().getUserName() );
        assertEquals( 
            expected.getUser().getUserPassword(),
            actual.getUser().getUserPassword() );
        assertEquals( 
            expected.getUser().getEmailAddress(),
            actual.getUser().getEmailAddress() );
    }

    /**
     * Test method for {@link JaxbRequestMapper#toPayload(IdentityManagerRequest)}.
     */
    @Test
    public void 
    testMappingStartListeningRequest() throws Exception
    {
        StartListeningRequest      expected = new StartListeningRequest();
        StartListeningRequest      actual   = null;        
        IUserIdentityEventListener listener = Mockito.mock( IUserIdentityEventListener.class ); 
                
        expected.setRequestId( "1" );
        expected.setSessionId( "0" );
        expected.setUserId( "123" );
        expected.setListener( listener );
        
        actual = 
            itsTarget.toRequest( 
                StartListeningRequest.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getRequestId(),actual.getRequestId() );
        assertEquals( expected.getSessionId(),actual.getSessionId() );
        assertEquals( expected.getUserId(),actual.getUserId() );
    }

    /**
     * Test method for {@link JaxbRequestMapper#toPayload(IdentityManagerRequest)}.
     */
    @Test
    public void 
    testMappingStopListeningRequest() throws Exception
    {
        StopListeningRequest expected = new StopListeningRequest();
        StopListeningRequest actual   = null;        
                
        expected.setRequestId( "1" );
        expected.setSessionId( "0" );
        expected.setUserId( "123" );
        
        actual = 
            itsTarget.toRequest( 
                StopListeningRequest.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getRequestId(),actual.getRequestId() );
        assertEquals( expected.getSessionId(),actual.getSessionId() );
        assertEquals( expected.getUserId(),actual.getUserId() );
    }
    
    /**
     * Test method for {@link JaxbRequestMapper#toPayload(IdentityManagerRequest)}.
     */
    @Ignore
    @Test
    public void 
    testMappingUserNamePasswordAuthenticateRequest() throws Exception
    {
        UserNamePasswordAuthenticateRequest expected = new UserNamePasswordAuthenticateRequest();
        UserNamePasswordAuthenticateRequest actual   = null;        
                
        expected.setRequestId( "1" );
        expected.setUserName( "JaneDoe" );
        expected.setUserPassword( "Foobaz" );
        
        actual = 
            itsTarget.toRequest( 
                UserNamePasswordAuthenticateRequest.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getRequestId(),actual.getRequestId() );
        assertEquals( expected.getUserName(),actual.getUserName() );
        assertEquals( expected.getUserPassword(),actual.getUserPassword() );
    }


}

// ##########################################################################
