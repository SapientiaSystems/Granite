// ##########################################################################
// # File Name:	MapperModule.java
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

package identitymanager.integration.mapper;

import strata1.injector.container.AbstractModule;
import strata1.injector.container.IContainer;
import strata1.injector.container.ThreadScope;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class MapperModule
    extends AbstractModule
{

    /************************************************************************
     * Creates a new MapperModule. 
     *
     */
    public 
    MapperModule()
    {
        super( "MapperModule" );
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
                bindType(IRequestMapper.class)
                    .toType(JaxbRequestMapper.class)
                    .withScope(new ThreadScope<IRequestMapper>()))
            .insertBinding( 
                bindType(IReplyMapper.class)
                    .toType(JaxbReplyMapper.class)
                    .withScope(new ThreadScope<IReplyMapper>()))
            .insertBinding( 
                bindType(IEventMapper.class)
                    .toType(JaxbEventMapper.class)
                    .withScope(new ThreadScope<IEventMapper>()));
    }

}

// ##########################################################################
