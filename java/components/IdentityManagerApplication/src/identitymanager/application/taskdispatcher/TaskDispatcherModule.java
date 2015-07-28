// ##########################################################################
// # File Name:	TaskDispatcherModule.java
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

package identitymanager.application.taskdispatcher;

import strata1.common.logger.ILogger;
import strata1.common.task.ITaskConsumer;
import strata1.common.task.ITaskDispatcher;
import strata1.common.task.TaskConsumer;
import strata1.common.task.TaskDisruptorDispatcher;
import strata1.injector.container.AbstractModule;
import strata1.injector.container.IContainer;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class TaskDispatcherModule 
    extends AbstractModule
{

    /************************************************************************
     * Creates a new TaskDispatcherModule. 
     *
     */
    public 
    TaskDispatcherModule()
    {
        super( "TaskDispatcherModule" );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    initialize(IContainer container)
    {
        ILogger         logger     = null;
        ITaskDispatcher dispatcher = null;
        ITaskConsumer   consumer1  = null;
        ITaskConsumer   consumer2  = null;
        ITaskConsumer   consumer3  = null;
        ITaskConsumer   consumer4  = null;
        
        logger     = container.getInstance( ILogger.class );
        dispatcher = new TaskDisruptorDispatcher(4096);
        consumer1  = new TaskConsumer(new AnyTaskSelector(),logger);
        consumer2  = new TaskConsumer(new AnyTaskSelector(),logger);
        consumer3  = new TaskConsumer(new AnyTaskSelector(),logger);
        consumer4  = new TaskConsumer(new AnyTaskSelector(),logger);
        
        dispatcher
            .attachConsumer( consumer1 )
            .attachConsumer( consumer2 )
            .attachConsumer( consumer3 )
            .attachConsumer( consumer4 );
        
        container
            .insertBinding( 
                bindType(ITaskConsumer.class)
                    .withKey( "Consumer1" )
                    .toInstance( consumer1 ) )
            .insertBinding( 
                bindType(ITaskConsumer.class)
                    .withKey( "Consumer2" )
                    .toInstance( consumer2 ) )
            .insertBinding( 
                bindType(ITaskConsumer.class)
                    .withKey( "Consumer3" )
                    .toInstance( consumer3 ) )
            .insertBinding( 
                bindType(ITaskConsumer.class)
                    .withKey( "Consumer4" )
                    .toInstance( consumer4 ) )
            .insertBinding( 
                bindType(ITaskDispatcher.class)
                    .toInstance( dispatcher ) );
    }

}

// ##########################################################################
