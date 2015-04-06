// ##########################################################################
// # File Name:	AuthenticationReplyReceiverTest.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerServiceTest Framework.
// #
// #   			The IdentityManagerServiceTest Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerServiceTest Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerServiceTest
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.service.authentication;

import static org.junit.Assert.*;
import identitymanager.service.subject.IRole;
import identitymanager.service.subject.PrincipalConstant;
import identitymanager.service.subject.ResourceActionPermission;
import identitymanager.service.user.IUser;
import identitymanager.service.user.UserName;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import strata1.common.datetime.DateTime;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class AuthenticationReplyReceiverTest
{
    private IAuthenticationReplyReceiver itsTarget;
    
    /************************************************************************
     *  
     *
     * @throws Exception
     */
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = new FakeAuthenticationReplyReceiver();
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
     * Test method for {@link IAuthenticationReplyReceiver#onAuthenticate(AuthenticateReply)}.
     */
    @Test
    public void 
    testOnAuthenticate() throws Exception
    {
        AuthenticateReply reply = new AuthenticateReply();
        IUser             user  = null;
        IRole             role  = null;
        
        reply.setOriginatingRequestId( "12345" );
        reply.setReplyId( "23571113" );
        reply.setDuplicate( false );
        reply.setTimestamp( DatatypeFactory.newInstance().newXMLGregorianCalendar( (GregorianCalendar)new DateTime().getCalendar() ) );
        reply.setStatus( AuthenticationStatus.SUCCEEDED );
        reply.setSubject( createSubjectDescriptor() );
        
        itsTarget.onAuthenticate( reply );
        
        assertTrue( itsTarget.hasSubject() );
        assertNotNull( itsTarget.getSubject() );
        
        assertTrue( itsTarget.getSubject() instanceof IUser );
        
        user = (IUser)itsTarget.getSubject();
        
        assertEquals( new UserName("johndoe"),user.getUserName() );
        assertEquals( new UserName("johndoe"),user.getPrincipal(PrincipalConstant.USER_NAME) );
        
        role = user.getRole( "RoleX" );
        
        assertNotNull( role );
        assertEquals( "RoleX",role.getName() );
        assertTrue( role.hasPermission( new ResourceActionPermission("X:*:read") ));
    }

    /**
     * Test method for {@link IAuthenticationReplyReceiver#onException(Exception)}.
     */
    @Test
    public void 
    testOnException() throws Exception
    {
        AuthenticationExceptionReply reply = new AuthenticationExceptionReply();
        
        reply.setOriginatingRequestId( "12345" );
        reply.setReplyId( "23571113" );
        reply.setDuplicate( false );
        reply.setTimestamp( DatatypeFactory.newInstance().newXMLGregorianCalendar( (GregorianCalendar)new DateTime().getCalendar() ) );
        reply.setStatus( AuthenticationStatus.FAILED );
        reply.setMessage( "Exception message." );
        
        try
        {
            itsTarget.onException( new AuthenticationException( reply.getMessage() ) );
        }
        catch (IllegalStateException e)
        {
            assertEquals( reply.getMessage(),e.getMessage() );
        }
    }

    /**
     * Test method for {@link IAuthenticationReplyReceiver#getSubject()}.
     */
    @Test
    public void 
    testGetSubject() throws Exception
    {
        AuthenticateReply reply = new AuthenticateReply();
        IUser             user  = null;
        IRole             role  = null;
        
        reply.setOriginatingRequestId( "12345" );
        reply.setReplyId( "23571113" );
        reply.setDuplicate( false );
        reply.setTimestamp( DatatypeFactory.newInstance().newXMLGregorianCalendar( (GregorianCalendar)new DateTime().getCalendar() ) );
        reply.setStatus( AuthenticationStatus.SUCCEEDED );
        reply.setSubject( createSubjectDescriptor() );
        
        itsTarget.onAuthenticate( reply );
        
        assertTrue( itsTarget.hasSubject() );
        assertNotNull( itsTarget.getSubject() );
    }

    /**
     * Test method for {@link IAuthenticationReplyReceiver#hasSubject()}.
     */
    @Test
    public void 
    testHasSubject() throws Exception
    {
        AuthenticateReply reply = new AuthenticateReply();
        IUser             user  = null;
        IRole             role  = null;
        
        reply.setOriginatingRequestId( "12345" );
        reply.setReplyId( "23571113" );
        reply.setDuplicate( false );
        reply.setTimestamp( DatatypeFactory.newInstance().newXMLGregorianCalendar( (GregorianCalendar)new DateTime().getCalendar() ) );
        reply.setStatus( AuthenticationStatus.SUCCEEDED );
        reply.setSubject( createSubjectDescriptor() );
        
        itsTarget.onAuthenticate( reply );
        
        assertTrue( itsTarget.hasSubject() );
        assertNotNull( itsTarget.getSubject() );
    }

    private SubjectDescriptor 
    createSubjectDescriptor()
    {
        UserDescriptor subject = new UserDescriptor();
        RoleDescriptor role    = new RoleDescriptor();
        PermissionDescriptor perm = new PermissionDescriptor();
        
        perm.setPermissionName( "X:*:*" );
        perm.setPermissionId( "1" );
        
        role.setRoleName( "RoleX" );
        role.getPermissions().add( perm );
        
        subject.setSessionId( "123" );
        subject.setUserName( "johndoe" );
        subject.setSubjectId( 1L );
        subject.getRoles().add( role );
        return subject;
    }

}

// ##########################################################################
