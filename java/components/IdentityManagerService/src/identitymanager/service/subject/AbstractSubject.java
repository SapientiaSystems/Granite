// ##########################################################################
// # File Name:	AbstractSubject.java
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
public abstract 
class AbstractSubject
    implements ISubject
{

    private static final long serialVersionUID = 1046684867391475026L;
    
    private final Map<String,IRole> itsRoles;

    /************************************************************************
     * Creates a new AbstractSubject. 
     *
     */
    protected 
    AbstractSubject()
    {
        itsRoles = new HashMap<String,IRole>();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IRole 
    getRole(String roleName)
    {
        return itsRoles.get( roleName );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Map<String,IRole> 
    getRoles()
    {
        return Collections.unmodifiableMap( itsRoles );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    hasRole(String roleName)
    {
        return itsRoles.containsKey( roleName );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    hasPermission(IPermission permission)
    {
        for (IRole role:itsRoles.values())
            if ( role.hasPermission( permission ) )
                return true;
        
        return false;
    }

    /************************************************************************
     *  
     *
     * @param role
     * @return
     */
    public AbstractSubject
    assignRole(IRole role)
    {
        if ( !hasRole( role.getName() ) )
            itsRoles.put( role.getName(),role );
        
        return this;
    }
}

// ##########################################################################
