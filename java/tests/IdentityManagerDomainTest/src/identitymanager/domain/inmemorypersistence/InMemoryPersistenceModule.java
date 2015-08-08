// ##########################################################################
// # File Name:	InMemoryPersistenceModule.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerDomainTest Framework.
// #
// #   			The IdentityManagerDomainTest Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerDomainTest Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerDomainTest
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.domain.inmemorypersistence;

import identitymanager.domain.persistence.PersistenceModule;
import identitymanager.domain.useridentity.IUserIdentityEventRepositoryProvider;
import identitymanager.domain.useridentity.IUserIdentityRepositoryProvider;
import strata1.persistence.repository.IRepositoryContext;
import javax.inject.Provider;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class InMemoryPersistenceModule
    extends PersistenceModule
{

    /************************************************************************
     * Creates a new InMemoryPersistenceModule. 
     *
     * @param name
     */
    public 
    InMemoryPersistenceModule()
    {
        super( "InMemoryPersistenceModule" );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    protected Provider<IRepositoryContext> 
    getRepositoryContextProvider()
    {
        return new InMemoryRepositoryContextProvider();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    protected Class<? extends IUserIdentityEventRepositoryProvider> 
    getUserIdentityEventRepositoryProviderType()
    {
        return InMemoryUserIdentityEventRepositoryProvider.class;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    protected Class<? extends IUserIdentityRepositoryProvider> 
    getUserIdentityRepositoryProviderType()
    {
        return InMemoryUserIdentityRepositoryProvider.class;
    }

}

// ##########################################################################
