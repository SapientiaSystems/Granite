// ##########################################################################
// # File Name:	IdentityManagerServerApp.java
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

import identitymanager.server.bootstrap.IdentityManagerServerFactory;
import strata1.injector.bootstrap.Bootstrapper;
import strata1.injector.bootstrap.IApplicationFactory;
import strata1.injector.bootstrap.IBootstrapper;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class IdentityManagerServerApp
{
    private final IApplicationFactory itsFactory;
    private final IBootstrapper       itsBootstrapper;
    
    /************************************************************************
     * Creates a new IdentityManagerServerApp. 
     *
     */
    public 
    IdentityManagerServerApp()
    {
        itsFactory      = new IdentityManagerServerFactory();
        itsBootstrapper = new Bootstrapper();
    }

    /************************************************************************
     *  
     *
     * @param arguments
     * @throws Exception
     */
    public void
    start()
        throws Exception
    {
        itsBootstrapper.run( itsFactory );
        itsBootstrapper
            .getStartStopController()
            .startApplication();
    }

    public void
    stop()
    {
        itsBootstrapper
            .getStartStopController()
            .stopApplication();
    }
}

// ##########################################################################
