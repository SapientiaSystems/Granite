// ##########################################################################
// # File Name:	User.java
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

import identitymanager.service.subject.AbstractSubject;
import identitymanager.service.subject.IPrincipal;
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
class User
    extends    AbstractSubject
    implements IUser
{

    private static final long serialVersionUID = 4145456935056751684L;
    
    private final UserName               itsUserName;
    private final Map<String,IPrincipal> itsPrincipals;

    /************************************************************************
     * Creates a new User. 
     *
     */
    public 
    User(final UserName userName)
    {
        itsUserName = userName;
        itsPrincipals = new HashMap<String,IPrincipal>();
        itsPrincipals.put( itsUserName.getKind(),itsUserName );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IPrincipal 
    getPrincipal(String principalKind)
    {
        if ( principalKind.equals( itsUserName.getKind() ))
            return itsUserName;
        
        return null;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Map<String,IPrincipal> 
    getPrincipals()
    {
        return Collections.unmodifiableMap( itsPrincipals );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    hasPrincipal(String principalKind)
    {
        return itsPrincipals.containsKey( principalKind );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public UserName 
    getUserName()
    {
        return itsUserName;
    }

}

// ##########################################################################
