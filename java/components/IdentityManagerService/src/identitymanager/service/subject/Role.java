// ##########################################################################
// # File Name:	Role.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerService Framework.
// #
// #   			The IdentityManagerService Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerService Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerService
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.service.subject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class Role
    implements IRole
{
    private static final long serialVersionUID = -593315161120514575L;
    
    private final String                  itsName;
    private final Map<String,IPermission> itsPermissions;
    
    /************************************************************************
     * Creates a new Role. 
     *
     */
    public 
    Role(String name)
    {
        itsName        = name;
        itsPermissions = new HashMap<String,IPermission>();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    getName()
    {
        return itsName;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Map<String,IPermission> 
    getPermissions()
    {
        return Collections.unmodifiableMap( itsPermissions );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    hasPermission(IPermission permission)
    {
        for (IPermission p : itsPermissions.values() )
            if ( p.implies( permission ) )
                return true;
        
        return false;
    }

    /************************************************************************
     *  
     *
     * @param permission
     * @return
     */
    public Role
    grantPermission(IPermission permission)
    {
        itsPermissions.put( permission.getName(),permission );
        return this;
    }
}

// ##########################################################################
