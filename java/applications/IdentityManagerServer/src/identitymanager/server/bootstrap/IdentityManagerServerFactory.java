// ##########################################################################
// # File Name:	IdentityManagerServerFactory.java
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

package identitymanager.server.bootstrap;

import identitymanager.application.taskdispatcher.TaskDispatcherModule;
import identitymanager.application.useridentity.UserIdentityAppModule;
import identitymanager.domain.useridentity.UserIdentityDomainModule;
import identitymanager.serverplatform.hibernatepersistence.HibernatePersistenceModule;
import identitymanager.serverplatform.javamail.JavaMailModule;
import identitymanager.serverplatform.jmsmessaging.JmsMessagingModule;
import identitymanager.common.utility.UtilityModule;
import indentitymanager.integration.serviceinvoker.ServiceInvokerModule;
import strata1.injector.bootstrap.AbstractApplicationFactory;
import strata1.injector.bootstrap.IStartStopController;
import strata1.injector.container.IModule;
import strata1.injector.property.PropertiesModule;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class IdentityManagerServerFactory
    extends AbstractApplicationFactory
{

    /************************************************************************
     * Creates a new IdentityManagerServerFactory. 
     *
     */
    public 
    IdentityManagerServerFactory() {}

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public List<IModule> 
    createModules()
    {
        List<IModule> modules = new ArrayList<IModule>();
        
        modules.add( new PropertiesModule( createProperties() ) );
        modules.add( new UtilityModule() );
        modules.add( new JmsMessagingModule() );
        modules.add( new HibernatePersistenceModule() );
        modules.add( new JavaMailModule() );
        modules.add( new UserIdentityDomainModule() );
        modules.add( new UserIdentityAppModule() );
        modules.add( new TaskDispatcherModule() );
//        modules.add( new TimerModule() );
        modules.add( new ServiceInvokerModule() );
        
        return modules;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IStartStopController 
    createStartStopController()
    {
        return null;
    }

    /************************************************************************
     *  
     *
     * @return
     */
    private Properties
    createProperties()
    {
        Properties  properties = new Properties();
        InputStream input = 
            ClassLoader
                .getSystemClassLoader()
                .getResourceAsStream( 
                    "identitymanager/server/main/identitymanagerserver.properties" );
        
        try
        {
            properties.load( input );
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
        
        return properties;
    }
    
}

// ##########################################################################
