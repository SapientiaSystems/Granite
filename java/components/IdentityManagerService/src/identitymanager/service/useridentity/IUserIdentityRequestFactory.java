// ##########################################################################
// # File Name:	IUserIdentityRequestFactory.java
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
interface IUserIdentityRequestFactory
{
    public StartCreatingUserIdentityRequest
    createStartCreatingUserIdentityRequest(UserIdentityData newUser);
    
    public FinishCreatingUserIdentityRequest
    createFinishCreatingUserIdentityRequest(
        Long   pendingEventId, 
        String verificationCode);
    
    public StartUpdatingUserIdentityRequest
    createStartUpdatingUserIdentityRequest(UserIdentityData updatedUser);
    
    public FinishUpdatingUserIdentityRequest
    createFinishUpdatingUserIdentityRequest(
        Long   pendingEventId, 
        String verificationCode);
    
    public UpdateUserIdentityRequest
    createUpdateUserIdentityRequest(UserIdentityData updatedUser);
    
    public StartListeningRequest
    createStartListeningRequest();
    
    public StopListeningRequest
    createStopListeningRequest();
}

// ##########################################################################
