// ##########################################################################
// # File Name:	IdentityManagerStartStopController.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerServer Framework.
// #
// #   			The IdentityManagerServer Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerServer Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerServer
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.server.main;

import indentitymanager.integration.serviceinvoker.ServiceInvoker;
import strata1.common.task.ITaskDispatcher;
import strata1.injector.bootstrap.IStartStopController;
import strata1.injector.container.IContainer;
import strata1.integrator.messaging.IMessagingSession;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class IdentityManagerStartStopController
    implements IStartStopController
{
    private IContainer itsContainer; 
    
    /************************************************************************
     * Creates a new IdentityManagerStartStopController. 
     *
     */
    public 
    IdentityManagerStartStopController() 
    {
        itsContainer = null;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IStartStopController 
    setContainer(IContainer container)
    {
        itsContainer = container;
        return this;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    startApplication()
    {
        IMessagingSession session1   = null;
        IMessagingSession session2   = null;
        ITaskDispatcher   dispatcher = null;
        ServiceInvoker    invoker1   = null;
        ServiceInvoker    invoker2   = null;
        //DeadSessionTimer  timer      = null;
        
        session1 = 
            itsContainer.getInstance(
                IMessagingSession.class,
                "CommandSession1" );
        session2 = 
            itsContainer.getInstance(
                IMessagingSession.class,
                "CommandSession2" );
        
        dispatcher = 
            itsContainer.getInstance( ITaskDispatcher.class );
        invoker1 = 
            itsContainer.getInstance(ServiceInvoker.class,"ServiceInvoker1");
        invoker2 = 
            itsContainer.getInstance(ServiceInvoker.class,"ServiceInvoker2");
        /*
        timer = 
            itsContainer.getInstance( DeadSessionTimer.class );
        */
        
        session1.startReceiving();
        session2.startReceiving();
        dispatcher.startDispatching();
        invoker1.startProducing();
        invoker2.startProducing();
        //timer.startProducing();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    stopApplication()
    {
        ITaskDispatcher  dispatcher = null;
        ServiceInvoker   invoker1   = null;
        ServiceInvoker   invoker2   = null;
        //DeadSessionTimer timer      = null;
        
        dispatcher = 
            itsContainer.getInstance( ITaskDispatcher.class );
        invoker1 = 
            itsContainer.getInstance(ServiceInvoker.class,"ServiceInvoker1");
        invoker2 = 
            itsContainer.getInstance(ServiceInvoker.class,"ServiceInvoker2");
        /*
        timer = 
            itsContainer.getInstance( DeadSessionTimer.class );
        */
        
        dispatcher.stopDispatching();
        invoker1.stopProducing();
        invoker2.stopProducing();
        //timer.stopProducing();
    }

}

// ##########################################################################
