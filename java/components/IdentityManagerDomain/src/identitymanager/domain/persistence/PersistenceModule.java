// ##########################################################################
// # File Name:	PersistenceModule.java
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

package identitymanager.domain.persistence;

import identitymanager.domain.useridentity.IUserIdentityEventRepositoryProvider;
import identitymanager.domain.useridentity.IUserIdentityRepositoryProvider;
import javax.inject.Provider;
import strata1.injector.container.AbstractModule;
import strata1.injector.container.IContainer;
import strata1.injector.container.SingletonScope;
import strata1.injector.container.ThreadScope;
import strata1.persistence.repository.IRepositoryContext;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public abstract 
class PersistenceModule
    extends AbstractModule
{

    /************************************************************************
     * Creates a new PersistenceModule. 
     *
     * @param name
     */
    public 
    PersistenceModule(String name)
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
                bindType(IRepositoryContext.class)
                    .toProvider(getRepositoryContextProvider())
                    .withScope( 
                        new SingletonScope<IRepositoryContext>()))
            .insertBinding( 
                bindType(IUserIdentityRepositoryProvider.class)
                    .toType(getUserIdentityRepositoryProviderType())
                    .withScope( 
                        new ThreadScope<IUserIdentityRepositoryProvider>()))
            .insertBinding( 
                bindType(IUserIdentityEventRepositoryProvider.class)
                    .toType(getUserIdentityEventRepositoryProviderType())
                    .withScope( 
                        new ThreadScope<IUserIdentityEventRepositoryProvider>()));  
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    protected abstract Provider<IRepositoryContext> 
    getRepositoryContextProvider();

    /************************************************************************
     *  
     *
     * @return
     */
    protected abstract Class<? extends IUserIdentityRepositoryProvider> 
    getUserIdentityRepositoryProviderType();
    
    /************************************************************************
     *  
     *
     * @return
     */
    protected abstract Class<? extends IUserIdentityEventRepositoryProvider> 
    getUserIdentityEventRepositoryProviderType();

}

// ##########################################################################
