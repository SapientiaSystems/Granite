// ##########################################################################
// # File Name:	IUserIdentityService.java
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

package identitymanager.service.useridentity;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
interface IUserIdentityService
{
    public void
    startCreatingUserIdentity(
        IUserIdentityReplyReceiver       receiver,
        StartCreatingUserIdentityRequest request);
    
    public void
    finishCreatingUserIdentity(
        IUserIdentityReplyReceiver        receiver,
        FinishCreatingUserIdentityRequest request);
    
    public void
    startUpdatingUserIdentity(
        IUserIdentityReplyReceiver       receiver,
        StartUpdatingUserIdentityRequest request);
    
    public void
    finishUpdatingUserIdentity(
        IUserIdentityReplyReceiver        receiver,
        FinishUpdatingUserIdentityRequest request);
    
    public void
    updateUserIdentity(
        IUserIdentityReplyReceiver receiver,
        UpdateUserIdentityRequest  request);
    
    public void
    startListening(
        IUserIdentityReplyReceiver receiver,
        StartListeningRequest      request);
    
    public void
    stopListening(
        IUserIdentityReplyReceiver receiver,
        StopListeningRequest       request);
    
    public boolean
    isListening();
}

// ##########################################################################
