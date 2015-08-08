// ##########################################################################
// # File Name:	UserIdentityServiceTest.java
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

package identitymanager.service.useridentity;

import static org.junit.Assert.*;
import identitymanager.application.useridentity.IUserIdentityFactory;
import identitymanager.application.useridentity.UserIdentityAppModule;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public abstract
class UserIdentityServiceTest
{
    private static IContainer           itsContainer;
    private IUserIdentityRequestFactory itsRequestFactory;
    private IUserIdentityFactory        itsReplyFactory;
    private IUserIdentityService        itsTarget;
    
    @BeforeClass
    public static void 
    setUpBeforeClass() throws Exception
    {
        itsContainer = new Container();
        new UserIdentityServiceModule().initialize( itsContainer );
        new UserIdentityAppModule().initialize( itsContainer );
    }

    @AfterClass
    public static void 
    tearDownAfterClass() throws Exception
    {
        itsContainer = null;
    }

    @Before
    public void 
    setUp() throws Exception
    {
        itsRequestFactory = itsContainer.getInstance( IUserIdentityRequestFactory.class );
        itsReplyFactory   = itsContainer.getInstance( IUserIdentityFactory.class );
        itsTarget = createTarget();
    }

    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget = null;
        itsRequestFactory = null;
        itsReplyFactory   = null;
    }

    @Test
    public void 
    testStartCreatingUserIdentity()
    {
        UserIdentityData                 newUser  = null;
        PersonNameData                   name     = null;
        SecurityQuestionData             question = null;
        StartCreatingUserIdentityRequest request  = null;
        StartCreatingUserIdentityReply   reply    = null;
        FakeUserIdentityReplyReceiver    receiver = null;
        
        newUser = new UserIdentityData();
        newUser.setUserName( "johndoe" );
        newUser.setUserPassword( "xxxxxxxxxxxxxxxxx" );
        newUser.setEmailAddress( "johndoe@email.com" );
        
        name = new PersonNameData();
        name.setFirstName( "John" );
        name.setLastName( "Doe" );
        
        newUser.setPersonName( name );
        
        question = new SecurityQuestionData();
        question.setQuestionId( 1 );
        question.setQuestion( "Mother's maiden name" );
        question.setAnswer( "Smith" );
        
        newUser.getSecurityQuestions().add( question );
        
        request = itsRequestFactory.createStartCreatingUserIdentityRequest( newUser );
        receiver = new FakeUserIdentityReplyReceiver();
        
        itsTarget.startCreatingUserIdentity( receiver,request );
        
        reply = itsReplyFactory.createUserIdentityReply( request );
        
        receiver.validateReply( reply );
    }

    @Test
    public void testFinishCreatingUserIdentity()
    {
        fail( "Not yet implemented" );
    }

    @Test
    public void testStartUpdatingUserIdentity()
    {
        fail( "Not yet implemented" );
    }

    @Test
    public void testFinishUpdatingUserIdentity()
    {
        fail( "Not yet implemented" );
    }

    @Test
    public void testUpdateUserIdentity()
    {
        fail( "Not yet implemented" );
    }

    @Test
    public void testStartListening()
    {
        fail( "Not yet implemented" );
    }

    @Test
    public void testStopListening()
    {
        fail( "Not yet implemented" );
    }

    @Test
    public void testIsListening()
    {
        fail( "Not yet implemented" );
    }

    protected abstract IUserIdentityService 
    createTarget();

}

// ##########################################################################
