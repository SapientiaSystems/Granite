// ##########################################################################
// # File Name:	InMemoryUserIdentityEventRepositoryProvider.java
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

import identitymanager.domain.useridentity.IUserIdentityEvent;
import identitymanager.domain.useridentity.IUserIdentityEventRepositoryProvider;
import identitymanager.common.utility.DefaultIdentifierGenerator;
import identitymanager.common.utility.IIdentifierGenerator;
import strata1.persistence.inmemoryrepository.IEntityReplicator;
import strata1.persistence.inmemoryrepository.InMemoryRepositoryContext;
import strata1.persistence.inmemoryrepository.InMemoryRepositoryProvider;
import strata1.persistence.repository.IKeyRetriever;
import strata1.persistence.repository.IRepositoryContext;
import javax.inject.Inject;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class InMemoryUserIdentityEventRepositoryProvider
    extends InMemoryRepositoryProvider<Long,IUserIdentityEvent>
    implements IUserIdentityEventRepositoryProvider
{
    private static final IIdentifierGenerator theirGenerator = 
        new DefaultIdentifierGenerator();
    
    /************************************************************************
     * Creates a new InMemoryUserIdentityEventRepositoryProvider. 
     *
     * @param context
     */
    @Inject
    public 
    InMemoryUserIdentityEventRepositoryProvider(
        IRepositoryContext context)
    {
        super( 
            IUserIdentityEvent.class,
            (InMemoryRepositoryContext)context,
            new IEntityReplicator<Long,IUserIdentityEvent>()
            {  
                {}
                @Override
                public Long 
                generateKey()
                {
                    return theirGenerator.getNextLongId();
                }

                @Override
                public IUserIdentityEvent 
                replicate(IUserIdentityEvent original)
                {
                    IUserIdentityEvent replicant = original.copy();
                    
                    if ( mustGenerateKey( replicant.getEventId() ) )
                        replicant.setEventId( generateKey() );
                    
                    return replicant;
                }
                
                private boolean
                mustGenerateKey(Long oldKey)
                {
                    return
                        (oldKey == null ) ||
                        (oldKey == 0L);
                }

            },
            new IKeyRetriever<Long,IUserIdentityEvent>()
            {
                {}
                @Override
                public Long 
                getKey(IUserIdentityEvent event)
                {
                    return event.getEventId();
                }   
            }); 
    }

}

// ##########################################################################
