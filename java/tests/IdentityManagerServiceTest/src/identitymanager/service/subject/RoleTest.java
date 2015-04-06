// ##########################################################################
// # File Name:	RoleTest.java
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
public 
class RoleTest
{
    private Role itsTarget;
    
    /************************************************************************
     *  
     *
     * @throws java.lang.Exception
     */
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = new Role("RoleX");
        
        itsTarget
            .grantPermission( new ResourceActionPermission("X:*:read") )
            .grantPermission( new ResourceActionPermission("X:y,z:write") );
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
     * Test method for {@link IRole#getName()}.
     */
    @Test
    public void 
    testGetName()
    {
        assertEquals("RoleX",itsTarget.getName());
    }

    /**
     * Test method for {@link IRole#getPermissions()}.
     */
    @Test
    public void 
    testGetPermissions()
    {
        Map<String,IPermission> permissions = itsTarget.getPermissions();
        
        assertEquals( 2,permissions.size() );
        
        assertTrue( permissions.containsKey( "X:*:read" ) );
        assertTrue( permissions.containsKey( "X:y,z:write" ) );
    }

    /**
     * Test method for {@link IRole#hasPermission(IPermission)}.
     */
    @Test
    public void 
    testHasPermissionIPermission()
    {        
        IPermission read  = new ResourceActionPermission( "X:*:read" );
        IPermission write = new ResourceActionPermission( "X:y,z:write" );
        IPermission all   = new ResourceActionPermission( "*:*:*" );

        assertTrue( itsTarget.hasPermission( read ));
        assertTrue( itsTarget.hasPermission( write ));
        assertFalse( itsTarget.hasPermission( all ));
    }

}

// ##########################################################################
