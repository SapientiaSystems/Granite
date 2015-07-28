// ##########################################################################
// # File Name:	IUserIdentityReplyFactory.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerApplication Framework.
// #
// #   			The IdentityManagerApplication Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerApplication Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerApplication
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.application.useridentity;

import identitymanager.domain.useridentity.IUserIdentity;
import identitymanager.domain.useridentity.IUserIdentityEvent;
import identitymanager.service.shared.UserIdentityDescriptor;
import identitymanager.service.useridentity.FinishCreatingUserIdentityReply;
import identitymanager.service.useridentity.FinishCreatingUserIdentityRequest;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityReply;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.StartCreatingUserIdentityReply;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import identitymanager.service.useridentity.StartUpdatingUserIdentityReply;
import identitymanager.service.useridentity.StartUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.UpdateUserIdentityReply;
import identitymanager.service.useridentity.UpdateUserIdentityRequest;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
interface IUserIdentityFactory
{
    public StartCreatingUserIdentityReply
    createUserIdentityReply(StartCreatingUserIdentityRequest request);
    
    public FinishCreatingUserIdentityReply
    createUserIdentityReply(FinishCreatingUserIdentityRequest request);
    
    public StartUpdatingUserIdentityReply
    createUserIdentityReply(StartUpdatingUserIdentityRequest request);
    
    public FinishUpdatingUserIdentityReply
    createUserIdentityReply(FinishUpdatingUserIdentityRequest request);
    
    public UpdateUserIdentityReply
    createUserIdentityReply(UpdateUserIdentityRequest request);
    
    public IUserIdentityEvent
    createUserIdentityEvent(StartCreatingUserIdentityRequest request);
    
    public IUserIdentity
    createUserIdentity(IUserIdentityEvent event);

    public UserIdentityDescriptor 
    createUserIdentityDescriptor(IUserIdentity identity);

}

// ##########################################################################
