// ##########################################################################
// # File Name:	InMemoryRepositoryContextProvider.java
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

import identitymanager.domain.useridentity.IUserIdentity;
import identitymanager.domain.useridentity.IUserIdentityEvent;
import strata1.persistence.inmemoryrepository.IPredicate;
import strata1.persistence.inmemoryrepository.InMemoryFinder;
import strata1.persistence.inmemoryrepository.InMemoryRepositoryContext;
import strata1.persistence.repository.IRepositoryContext;
import javax.inject.Provider;
import java.util.Map;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class InMemoryRepositoryContextProvider
    implements Provider<IRepositoryContext>
{

    /************************************************************************
     * Creates a new InMemoryRepositoryContextProvider. 
     *
     */
    public 
    InMemoryRepositoryContextProvider()
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IRepositoryContext 
    get()
    {
        InMemoryRepositoryContext context = new InMemoryRepositoryContext();
        
        new InMemoryFinder<IUserIdentityEvent>(
            context, 
            "getUserIdentityEventWithUserName", 
            IUserIdentityEvent.class, 
            new IPredicate<IUserIdentityEvent>() 
            {
                {}
                
                @Override
                public boolean 
                evaluate(IUserIdentityEvent event,Map<String,Object> parameters)
                {
                    String userName = (String)parameters.get( "userName" );
                    
                    return event.getUserName().equals(userName);
                }
            });
        
        new InMemoryFinder<IUserIdentityEvent>(
            context, 
            "hasUserIdentityEventWithUserName", 
            IUserIdentityEvent.class, 
            new IPredicate<IUserIdentityEvent>() 
            {
                {}
                
                @Override
                public boolean 
                evaluate(IUserIdentityEvent event,Map<String,Object> parameters)
                {
                    String userName = (String)parameters.get( "userName" );
                    
                    return event.getUserName().equals(userName);
                }
            });
        
        new InMemoryFinder<IUserIdentity>(
            context, 
            "getUserIdentityWithUserName", 
            IUserIdentity.class, 
            new IPredicate<IUserIdentity>() 
            {
                {}
                
                @Override
                public boolean 
                evaluate(IUserIdentity user,Map<String,Object> parameters)
                {
                    String userName = (String)parameters.get( "userName" );
                    
                    return user.getUserName().equals(userName);
                }
            });
        
        new InMemoryFinder<IUserIdentity>(
            context, 
            "hasUserIdentityWithUserName", 
            IUserIdentity.class, 
            new IPredicate<IUserIdentity>() 
            {
                {}
                
                @Override
                public boolean 
                evaluate(IUserIdentity user,Map<String,Object> parameters)
                {
                    String userName = (String)parameters.get( "userName" );
                    
                    return user.getUserName().equals(userName);
                }
            });

        return context;
    }

}

// ##########################################################################
