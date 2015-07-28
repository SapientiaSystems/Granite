// ##########################################################################
// # File Name:	MessagingModule.java
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

package identitymanager.integration.messaging;

import strata1.injector.container.AbstractModule;
import strata1.injector.container.IContainer;
import strata1.integrator.messaging.IMessagingSession;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public abstract 
class MessagingModule 
    extends AbstractModule
{

    /************************************************************************
     * Creates a new MessagingModule. 
     *
     * @param name
     */
    public 
    MessagingModule(String name)
    {
        super( name );
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
                bindType(IMessagingSession.class)
                    .withKey( "CommandSession1" )
                    .toInstance(createCommandSession1()))
            .insertBinding( 
                bindType(IMessagingSession.class)
                    .withKey( "EventSession1" )
                    .toInstance(createEventSession1()))
            .insertBinding( 
                bindType(IMessagingSession.class)
                    .withKey( "CommandSession2" )
                    .toInstance(createCommandSession2()))
            .insertBinding( 
                bindType(IMessagingSession.class)
                    .withKey( "EventSession2" )
                    .toInstance(createEventSession2()));
    }

    /************************************************************************
     *  
     *
     * @return
     */
    protected abstract IMessagingSession 
    createCommandSession1();

    /************************************************************************
     *  
     *
     * @return
     */
    protected abstract IMessagingSession 
    createEventSession1();

    /************************************************************************
     *  
     *
     * @return
     */
    protected abstract IMessagingSession 
    createCommandSession2();

    /************************************************************************
     *  
     *
     * @return
     */
    protected abstract IMessagingSession 
    createEventSession2();

}

// ##########################################################################
