// ##########################################################################
// # File Name:	EmailAppModule.java
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

package identitymanager.application.email;

import identitymanager.domain.useridentity.IUserIdentityRepositoryProvider;
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
public abstract 
class EmailAppModule
    extends AbstractModule
{

    /************************************************************************
     * Creates a new EmailAppModule. 
     *
     * @param name
     */
    protected 
    EmailAppModule(String name)
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
                bindType(IEmailSession.class)
                    .toProvider(getEmailSessionProvider(container))
                    .withScope(new ThreadScope<IEmailSession>()));
    }

    /************************************************************************
     *  
     *
     * @return
     */
    protected abstract IEmailSessionProvider 
    getEmailSessionProvider(IContainer container);
}

// ##########################################################################
