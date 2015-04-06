// ##########################################################################
// # File Name:	PermissionTest.java
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
class PermissionTest
{
    private IPermission itsTarget;
    
    /************************************************************************
     *  
     *
     * @throws java.lang.Exception
     */
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = new ResourceActionPermission("*:*: * ");
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

    @Test
    public void 
    testEquals1()
    {
        IPermission perm1 = new ResourceActionPermission( "X:x,y,z:a,b" );
        IPermission perm2 = new ResourceActionPermission( "X:x,y,z:a,b" );
        
        assertTrue( perm1.equals( perm2 ) );
        assertTrue( perm2.equals( perm1 ) );
    }

    @Test
    public void 
    testEquals2()
    {
        IPermission perm1 = new ResourceActionPermission( "X:x,y,z:a,b" );
        IPermission perm2 = new ResourceActionPermission( "Y:x,y,z:a,b" );
        
        assertFalse( perm1.equals( perm2 ) );
        assertFalse( perm2.equals( perm1 ) );
    }

    @Test
    public void 
    testHasCode1()
    {
        IPermission perm1 = new ResourceActionPermission( "X:x,y,z:a,b" );
        IPermission perm2 = new ResourceActionPermission( "X:x,y,z:a,b" );
        
        assertEquals( perm1.hashCode(),perm2.hashCode() );
    }

    @Test
    public void 
    testHashCode2()
    {
        IPermission perm1 = new ResourceActionPermission( "X:x,y,z:a,b" );
        IPermission perm2 = new ResourceActionPermission( "Y:x,y,z:a,b" );
        
        assertFalse( perm1.hashCode() == perm2.hashCode() );
    }

    /**
     * Test method for {@link IPermission#getName()}.
     */
    @Test
    public void 
    testGetName()
    {
        assertEquals( "*:*:*",itsTarget.getName() );
    }

    /**
     * Test method for {@link IPermission#implies(IPermission)}.
     */
    @Test
    public void 
    testImplies1()
    {
        IPermission permission = new ResourceActionPermission( "X:x,y,z:*" );
        
        assertTrue( itsTarget.implies( permission ) );
        assertFalse( permission.implies( itsTarget ) );
    }
    
    @Test
    public void
    testImplies2()
    {
        IPermission general  = new ResourceActionPermission( "X:x,y,z:*" );
        IPermission specific = new ResourceActionPermission( "X:x,y,z:a" );
        
        assertTrue( general.implies( specific ) );
        assertFalse( specific.implies( general ) );
    }
    
    @Test
    public void
    testImplies3()
    {
        IPermission perm1 = new ResourceActionPermission( "X,Y:x,y,z:a,b" );
        IPermission perm2 = new ResourceActionPermission( "X,Y:x,y,z:a,b" );
        
        assertTrue( perm1.implies( perm2 ) );
        assertTrue( perm2.implies( perm1 ) );
    }

    @Test
    public void
    testImplies4()
    {
        IPermission perm1  = new ResourceActionPermission( "Type1:instance1:action1" );
        IPermission perm2  = new ResourceActionPermission( "Type2:instance1:action1" );
        
        assertFalse( perm1.implies( perm2 ) );
        assertFalse( perm2.implies( perm1 ) );
    }

    @Test
    public void
    testImplies5()
    {
        IPermission perm1  = new ResourceActionPermission( "Type1:instance2:action1" );
        IPermission perm2  = new ResourceActionPermission( "Type1:instance1:action1" );
        
        assertFalse( perm1.implies( perm2 ) );
        assertFalse( perm2.implies( perm1 ) );
    }

    @Test
    public void
    testImplies6()
    {
        IPermission perm1  = new ResourceActionPermission( "Type1:instance1:action1" );
        IPermission perm2  = new ResourceActionPermission( "Type1:instance1:action2" );
        
        assertFalse( perm1.implies( perm2 ) );
        assertFalse( perm2.implies( perm1 ) );
    }

    @Test
    public void
    testImplies7()
    {
        IPermission perm1  = new ResourceActionPermission( "*:instance1:*" );
        IPermission perm2  = new ResourceActionPermission( "Type1:*:action2" );
        
        assertFalse( perm1.implies( perm2 ) );
        assertFalse( perm2.implies( perm1 ) );
    }

}

// ##########################################################################
