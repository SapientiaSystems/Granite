// ##########################################################################
// # File Name:	UserName.java
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

package identitymanager.service.user;

import identitymanager.service.subject.IPrincipal;
import identitymanager.service.subject.PrincipalConstant;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserName
    implements IPrincipal
{
    private static final long serialVersionUID = -9209201702663173715L;
    private final String      itsUserName;
    
    /************************************************************************
     * Creates a new UserName. 
     *
     */
    public 
    UserName(final String userName)
    {
        itsUserName = userName;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    equals(Object other)
    {
        if ( other instanceof UserName )
            return itsUserName.equals( ((UserName)other).itsUserName );
        
        return false;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    hashCode()
    {
        int hash = 31;
        
        hash = hash + 37 * itsUserName.hashCode();
        return hash;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    toString()
    {
        return itsUserName;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    getKind()
    {
        return PrincipalConstant.USER_NAME;
    }

}

// ##########################################################################
