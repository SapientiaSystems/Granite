// ##########################################################################
// # File Name:	UserIdentityDomainModule.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerDomain Framework.
// #
// #   			The IdentityManagerDomain Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerDomain Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerDomain
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.domain.useridentity;

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
class UserIdentityDomainModule
    extends AbstractModule
{

    /************************************************************************
     * Creates a new UserIdentityDomainModule. 
     *
     * @param name
     */
    public 
    UserIdentityDomainModule()
    {
        super( "UserIdentityDomainModule" );
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
                bindType(IUserIdentityEventRepository.class)
                    .toType(UserIdentityEventRepository.class)
                    .withScope(
                        new ThreadScope<IUserIdentityEventRepository>()))
            .insertBinding( 
                bindType(IUserIdentityRepository.class)
                    .toType(UserIdentityRepository.class)
                    .withScope(new ThreadScope<IUserIdentityRepository>()));
    }

}

// ##########################################################################
