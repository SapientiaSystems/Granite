// ##########################################################################
// # File Name:	JaxbReplyMapperTest.java
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
import identitymanager.service.authentication.AuthenticateReply;
import identitymanager.service.authentication.PermissionDescriptor;
import identitymanager.service.authentication.RoleDescriptor;
import identitymanager.service.authentication.UserDescriptor;
import identitymanager.service.shared.IdentityManagerReply;
import identitymanager.service.shared.OperationStatus;
import identitymanager.service.useridentity.StartCreatingUserIdentityReply;
import identitymanager.service.useridentity.StartListeningReply;
import identitymanager.service.useridentity.StopListeningReply;
import identitymanager.service.useridentity.UpdateUserIdentityReply;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class JaxbReplyMapperTest
{
    private IReplyMapper itsTarget;
    
    /************************************************************************
     *  
     *
     * @throws java.lang.Exception
     */
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = new JaxbReplyMapper();
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
     * Test method for {@link JaxbReplyMapper#toPayload(IdentityManagerReply)}.
     */
    @Test
    public void 
    testMappingStartCreatingUserIdentityReply() throws Exception
    {
        StartCreatingUserIdentityReply expected     = new StartCreatingUserIdentityReply();
        StartCreatingUserIdentityReply actual       = null;
        
        expected.setReplyId( "1" );
        expected.setOriginatingRequestId( "1" );
        expected.setSessionId( "0" );
        expected.setStatus( OperationStatus.SUCCEEDED );
        
        actual = 
            itsTarget.toReply( 
                StartCreatingUserIdentityReply.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getReplyId(),actual.getReplyId() );
        assertEquals( expected.getOriginatingRequestId(),actual.getOriginatingRequestId() );
        assertEquals( expected.getSessionId(),actual.getSessionId() );
        assertEquals( expected.getStatus(),actual.getStatus() );
    }

    /**
     * Test method for {@link JaxbReplyMapper#toPayload(IdentityManagerReply)}.
     */
    @Test
    public void 
    testMappingUpdateUserIdentityReply() throws Exception
    {
        UpdateUserIdentityReply expected     = new UpdateUserIdentityReply();
        UpdateUserIdentityReply actual       = null;
        
        expected.setReplyId( "1" );
        expected.setOriginatingRequestId( "1" );
        expected.setSessionId( "0" );
        expected.setStatus( OperationStatus.FAILED );
        expected.setMessage( "Update failed." );
        
        actual = 
            itsTarget.toReply( 
                UpdateUserIdentityReply.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getReplyId(),actual.getReplyId() );
        assertEquals( expected.getOriginatingRequestId(),actual.getOriginatingRequestId() );
        assertEquals( expected.getSessionId(),actual.getSessionId() );
        assertEquals( expected.getStatus(),actual.getStatus() );
        assertEquals( expected.getMessage(),actual.getMessage() );
    }

    /**
     * Test method for {@link JaxbReplyMapper#toPayload(IdentityManagerReply)}.
     */
    @Test
    public void 
    testMappingStartListeningReply() throws Exception
    {
        StartListeningReply expected = new StartListeningReply();
        StartListeningReply actual   = null;        
                
        expected.setReplyId( "1" );
        expected.setOriginatingRequestId( "1" );
        expected.setSessionId( "0" );
        expected.setStatus( OperationStatus.SUCCEEDED );
        
        actual = 
            itsTarget.toReply( 
                StartListeningReply.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getReplyId(),actual.getReplyId() );
        assertEquals( expected.getOriginatingRequestId(),actual.getOriginatingRequestId() );
        assertEquals( expected.getSessionId(),actual.getSessionId() );
        assertEquals( expected.getStatus(),actual.getStatus() );
    }

    /**
     * Test method for {@link JaxbReplyMapper#toPayload(IdentityManagerReply)}.
     */
    @Test
    public void 
    testMappingStopListeningReply() throws Exception
    {
        StopListeningReply expected = new StopListeningReply();
        StopListeningReply actual   = null;        
                
        expected.setReplyId( "1" );
        expected.setOriginatingRequestId( "1" );
        expected.setSessionId( "0" );
        expected.setStatus( OperationStatus.SUCCEEDED );
        
        actual = 
            itsTarget.toReply( 
                StopListeningReply.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getReplyId(),actual.getReplyId() );
        assertEquals( expected.getOriginatingRequestId(),actual.getOriginatingRequestId() );
        assertEquals( expected.getSessionId(),actual.getSessionId() );
        assertEquals( expected.getStatus(),actual.getStatus() );
    }
    
    /**
     * Test method for {@link JaxbReplyMapper#toPayload(IdentityManagerReply)}.
     */
    @Ignore
    @Test
    public void 
    testMappingAuthenticateReply() throws Exception
    {
        AuthenticateReply    expected = new AuthenticateReply();
        AuthenticateReply    actual   = null;
        UserDescriptor       user     = new UserDescriptor();
        RoleDescriptor       role     = new RoleDescriptor();
        PermissionDescriptor perm     = new PermissionDescriptor();
        
        perm.setPermissionId( "1" );
        perm.setPermissionName( "X:*:read,write" );
        role.setRoleId( 2 );
        role.setRoleName( "X-Admin" );
        role.getPermissions().add( perm );
        user.getRoles().add( role );
        user.setUserName( "JohnDoe" );
        
        expected.setReplyId( "1" );
        expected.setOriginatingRequestId( "2" );
        expected.setSubject( user );
        expected.setAuthenticated( true );
        expected.setStatus( OperationStatus.SUCCEEDED );
        
        actual = 
            itsTarget.toReply( 
                AuthenticateReply.class,
                itsTarget.toPayload( expected ) );
        
        assertEquals( expected.getReplyId(),actual.getReplyId() );
        assertEquals( expected.getOriginatingRequestId(),actual.getOriginatingRequestId() );
        assertEquals( expected.isAuthenticated(),actual.isAuthenticated() );
        assertEquals( expected.getStatus(),actual.getStatus() );
        assertEquals( expected.getSubject().getSubjectId(),actual.getSubject().getSubjectId() );
        assertEquals( expected.getSubject().getRoles().get( 0 ).getRoleName(),actual.getSubject().getRoles().get( 0 ).getRoleName() );
    }


}

// ##########################################################################
