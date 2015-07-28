// ##########################################################################
// # File Name:	IUserIdentityProcessor.java
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
import identitymanager.domain.useridentity.VerificationException;
import strata1.persistence.repository.InsertFailedException;
import strata1.persistence.repository.InvalidInputException;
import strata1.persistence.repository.NotUniqueException;
import strata1.persistence.repository.UpdateFailedException;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
interface IUserIdentityProcessor
{
    public StartCreatingUserIdentityReply
    startCreatingUserIdentity(StartCreatingUserIdentityRequest request) 
        throws 
            InvalidInputException,
            NotUniqueException,
            InsertFailedException;
    
    public FinishCreatingUserIdentityReply
    finishCreatingUserIdentity(FinishCreatingUserIdentityRequest request)
        throws 
            InvalidInputException,
            NotUniqueException,
            InsertFailedException, 
            VerificationException, 
            UpdateFailedException;
    
    public StartUpdatingUserIdentityReply
    startUpdatingUserIdentity(StartUpdatingUserIdentityRequest request)
        throws 
            InvalidInputException,
            NotUniqueException,
            UpdateFailedException;
    
    public FinishUpdatingUserIdentityReply
    finishUpdatingUserIdentity(FinishUpdatingUserIdentityRequest request)
        throws 
            InvalidInputException,
            NotUniqueException,
            UpdateFailedException;
     
    public UpdateUserIdentityReply
    updateUserIdentity(UpdateUserIdentityRequest request)
        throws 
            InvalidInputException,
            NotUniqueException,
            UpdateFailedException;
}

// ##########################################################################
