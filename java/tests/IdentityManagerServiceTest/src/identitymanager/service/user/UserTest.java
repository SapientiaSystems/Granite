// ##########################################################################
// # File Name:	UserTest.java
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

package identitymanager.service.user;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import identitymanager.service.subject.IPrincipal;
import identitymanager.service.subject.ISubject;
import identitymanager.service.subject.PrincipalConstant;
import identitymanager.service.subject.SubjectTest;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserTest
    extends SubjectTest
{
 
    /************************************************************************
     * {@inheritDoc} 
     */
    @Before
    public void 
    setUp() throws Exception
    {
        super.setUp();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @After
    public void 
    tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test method for {@link identitymanager.service.user.IUser#getUserName()}.
     */
    @Test
    public void 
    testGetUserName()
    {
        IUser user = (IUser)getTarget();
        
        assertEquals( new UserName("johndoe"),user.getUserName() );
    }

    /**
     * Test method for {@link ISubject#getPrincipal(String)}.
     */
    @Test
    public void 
    testGetPrincipal()
    {
        assertEquals( 
            new UserName("johndoe"),
            getTarget().getPrincipal( PrincipalConstant.USER_NAME ));
    }

    /**
     * Test method for {@link ISubject#getPrincipals()}.
     */
    @Test
    public void 
    testGetPrincipals()
    {
        Map<String,IPrincipal> principals = getTarget().getPrincipals();
        
        assertEquals( 1,principals.size() );
        assertTrue( principals.containsKey( PrincipalConstant.USER_NAME ));
        assertFalse( principals.containsKey( PrincipalConstant.PUBLIC_KEY ));
    }

    /**
     * Test method for {@link ISubject#hasPrincipal(String)}.
     */
    @Test
    public void 
    testHasPrincipal()
    {
        assertTrue( getTarget().hasPrincipal( PrincipalConstant.USER_NAME ));
        assertFalse( getTarget().hasPrincipal( PrincipalConstant.PUBLIC_KEY ));
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    protected ISubject 
    createTarget()
    {
        return new User(new UserName("johndoe"));
    }

}

// ##########################################################################
