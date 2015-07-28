// ##########################################################################
// # File Name:	IUserIdentityRepository.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerDomain Framework.
// #
// #   			The IdentityManagerDomain Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerDomain Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerDomain
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.domain.useridentity;

import strata1.persistence.repository.IRepository;
import strata1.persistence.repository.InsertFailedException;
import strata1.persistence.repository.InvalidInputException;
import strata1.persistence.repository.NotUniqueException;
import strata1.persistence.repository.RemoveFailedException;
import strata1.persistence.repository.UpdateFailedException;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
interface IUserIdentityEventRepository
    extends IRepository
{
    public IUserIdentityEvent
    insertUserIdentityEvent(IUserIdentityEvent newEvent)
        throws InsertFailedException;
    
    public IUserIdentityEvent
    updateUserIdentityEvent(IUserIdentityEvent existingEvent)
        throws UpdateFailedException;
    
    public void
    removeUserIdentityEvent(IUserIdentityEvent existingEvent)
        throws RemoveFailedException;
    
    public IUserIdentityEvent
    getUserIdentityEvent(Long eventId);
    
    public IUserIdentityEvent
    getUserIdentityEventWithUserName(String userName) 
        throws InvalidInputException,NotUniqueException;
    
    public boolean
    hasUserIdentityEvent(Long eventId);
    
    public boolean
    hasUserIdentityEventWithUserName(String userName)
        throws InvalidInputException,NotUniqueException;
}

// ##########################################################################
