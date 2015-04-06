// ##########################################################################
// # File Name:	SubjectTest.java
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

package identitymanager.service.subject;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public abstract 
class SubjectTest
{
    private ISubject itsTarget;
    
    /************************************************************************
     *  
     *
     * @throws java.lang.Exception
     */
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = createTarget();
        assignRoles( createRoles() );
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
     * Test method for {@link ISubject#getRole(String)}.
     */
    @Test
    public void 
    testGetRole()
    {
        assertNotNull( itsTarget.getRole( "RoleA" ) );
        assertNotNull( itsTarget.getRole( "RoleB" ) );
    }

    /**
     * Test method for {@link ISubject#getRoles()}.
     */
    @Test
    public void 
    testGetRoles()
    {
        Map<String,IRole> roles = itsTarget.getRoles();
        
        assertEquals( 2,roles.size() );
        assertTrue( roles.containsKey( "RoleA" ) );
        assertTrue( roles.containsKey( "RoleB" ) );
        
    }


    /**
     * Test method for {@link ISubject#hasRole(String)}.
     */
    @Test
    public void 
    testHasRole()
    {
        assertTrue( itsTarget.hasRole( "RoleA" ));
        assertTrue( itsTarget.hasRole( "RoleB" ));
        assertFalse( itsTarget.hasRole( "RoleC" ));
    }

    /**
     * Test method for {@link ISubject#hasPermission(IPermission)}.
     */
    @Test
    public void 
    testHasPermission()
    {
        IPermission readA   = new ResourceActionPermission("A:*:read");
        IPermission writeA  = new ResourceActionPermission("A:*:write");
        IPermission createA = new ResourceActionPermission("A:*:create");
        IPermission allA    = new ResourceActionPermission("A:*:*");
        
        assertTrue( itsTarget.hasPermission( readA ) );
        assertTrue( itsTarget.hasPermission( writeA ) );
        assertFalse( itsTarget.hasPermission( createA ) );
        assertFalse( itsTarget.hasPermission( allA ) );
    }
    
    protected ISubject
    getTarget()
    {
        return itsTarget;
    }
    
    protected abstract ISubject
    createTarget();

    private void 
    assignRoles(List<IRole> roles)
    {
        for (IRole role:roles)
            ((AbstractSubject)itsTarget).assignRole( role );
    }

    private List<IRole> 
    createRoles()
    {
        Role        roleA = new Role( "RoleA" );
        Role        roleB = new Role( "RoleB" );
        List<IRole> roles = new ArrayList<IRole>();
        
        roleA.grantPermission( new ResourceActionPermission("A:*:read,write") );
        roleB.grantPermission( new ResourceActionPermission("B:*:create,read,write") );
        
        roles.add(  roleA );
        roles.add(  roleB );
        
        return roles;
    }
}

// ##########################################################################
