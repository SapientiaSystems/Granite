// ##########################################################################
// # File Name:	CreateUserIdentityTask.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerIntegration Framework.
// #
// #   			The IdentityManagerIntegration Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerIntegration Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerIntegration
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package indentitymanager.integration.serviceinvoker;

import identitymanager.service.useridentity.FinishCreatingUserIdentityRequest;
import identitymanager.service.useridentity.IUserIdentityReplyReceiver;
import identitymanager.service.useridentity.IUserIdentityService;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import strata1.common.logger.ILogger;
import strata1.common.task.AbstractTask;
import strata1.common.task.ITask;
import strata1.injector.container.IContainer;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class FinishCreatingUserIdentityTask
    extends    AbstractTask
    implements ITask
{
    private final IContainer                        itsContainer;
    private final FinishCreatingUserIdentityRequest itsRequest;
    private final IUserIdentityReplyReceiver        itsReceiver;

    /************************************************************************
     * Creates a new CreateUserIdentityTask. 
     *
     * @param container
     * @param request
     * @param receiver
     */
    public 
    FinishCreatingUserIdentityTask(
        IContainer                        container,
        FinishCreatingUserIdentityRequest request,
        IUserIdentityReplyReceiver        receiver)
    {
        super( "FinishCreatingUserIdentityTask" );
        itsContainer = container;
        itsRequest   = request;
        itsReceiver  = receiver;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    execute()
    {
        IUserIdentityService service = null;
        ILogger              logger  = null;
       
        service = 
            itsContainer.getInstance( 
                IUserIdentityService.class,
                "UserIdentityServiceImplementation" );
        logger = 
            itsContainer.getInstance( ILogger.class );
        
        logger.logVerbose( 
            "Executing finish creating useridentity task for request: " + 
            itsRequest.getRequestId() );        
        
        service.finishCreatingUserIdentity( itsReceiver,itsRequest );
    }
}

// ##########################################################################
