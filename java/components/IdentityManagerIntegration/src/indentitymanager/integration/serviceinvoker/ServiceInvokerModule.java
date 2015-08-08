// ##########################################################################
// # File Name:	ServiceInvokerModule.java
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

import strata1.injector.container.AbstractModule;
import strata1.injector.container.IContainer;
import strata1.injector.container.SingletonScope;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class ServiceInvokerModule 
    extends AbstractModule
{

    /************************************************************************
     * Creates a new ServiceInvokerModule. 
     *
     */
    public 
    ServiceInvokerModule()
    {
        super( "ServiceInvokerModule" );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    initialize(IContainer container)
    {
        container
            .insertBinding( 
                bindType(ServiceInvoker.class)
                    .withKey( "ServiceInvoker1" )
                    .toProvider( 
                        new ServiceInvokerProvider(
                            container,
                            "CommandSession1",
                            "RequestChannelId1",
                            "ReplyChannelId1",
                            "EventSession1",
                            "EventChannelId1") )
                    .withScope( new SingletonScope<ServiceInvoker>() ) )
            .insertBinding( 
                bindType(ServiceInvoker.class)
                    .withKey( "ServiceInvoker2" )
                    .toProvider( 
                        new ServiceInvokerProvider(
                            container,
                            "CommandSession2",
                            "RequestChannelId2",
                            "ReplyChannelId2",
                            "EventSession2",
                            "EventChannelId2") )
                    .withScope( new SingletonScope<ServiceInvoker>() ) );
    }
}

// ##########################################################################
