// ##########################################################################
// # File Name:	UtilityModule.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerCommon Framework.
// #
// #   			The IdentityManagerCommon Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerCommon Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerCommon
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.common.utility;

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
class UtilityModule
    extends AbstractModule
{

    /************************************************************************
     * Creates a new UtilityModule. 
     *
     * @param name
     */
    public 
    UtilityModule()
    {
        super( "UtilityModule" );
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
                bindType(IIdentifierGenerator.class)
                    .toType(DefaultIdentifierGenerator.class)
                    .withScope(new SingletonScope<IIdentifierGenerator>()))
            .insertBinding( 
                bindType(ITimestampGenerator.class)
                    .toType(DefaultTimestampGenerator.class)
                    .withScope(new SingletonScope<ITimestampGenerator>()));
    }

}

// ##########################################################################
