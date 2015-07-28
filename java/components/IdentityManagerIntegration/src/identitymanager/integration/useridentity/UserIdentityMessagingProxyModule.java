// ##########################################################################
// # File Name:	UserIdentityMessagingProxyModule.java
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

package identitymanager.integration.useridentity;

import identitymanager.service.useridentity.IUserIdentityService;
import identitymanager.integration.messagingproxy.MessagingProxyModule;
import strata1.injector.container.IContainer;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentityMessagingProxyModule
    extends MessagingProxyModule
{

    /************************************************************************
     * Creates a new UserIdentityMessagingProxyModule. 
     *
     * @param name
     */
    public 
    UserIdentityMessagingProxyModule()
    {
        super( "UserIdentityMessagingProxyModule" );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    protected void 
    createProxyBinding(IContainer container)
    {
        container
            .insertBinding(
                bindType(IUserIdentityService.class)
                    .toProvider( 
                        new UserIdentityMessagingProxyProvider(container)));  
    }

}

// ##########################################################################
