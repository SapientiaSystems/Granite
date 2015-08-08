// ##########################################################################
// # File Name:	InMemoryUserIdentityRepositoryProvider.java
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
import identitymanager.domain.useridentity.IUserIdentityRepositoryProvider;
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
class InMemoryUserIdentityRepositoryProvider
    extends InMemoryRepositoryProvider<String,IUserIdentity>
    implements IUserIdentityRepositoryProvider
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
    InMemoryUserIdentityRepositoryProvider(
        IRepositoryContext context)
    {
        super( 
            IUserIdentity.class,
            (InMemoryRepositoryContext)context,
            new IEntityReplicator<String,IUserIdentity>()
            {  
                {}
                @Override
                public String 
                generateKey()
                {
                    return theirGenerator.getNextStringId();
                }

                @Override
                public IUserIdentity
                replicate(IUserIdentity original)
                {
                    IUserIdentity replicant = original.copy();
                    
                    if ( mustGenerateKey( replicant.getUserId() ) )
                        replicant.setUserId( generateKey() );
                    
                    return replicant;
                }
                
                private boolean
                mustGenerateKey(String oldKey)
                {
                    return
                        (oldKey == null ) ||
                        (oldKey.isEmpty() );
                }

            },
            new IKeyRetriever<String,IUserIdentity>()
            {
                {}
                @Override
                public String 
                getKey(IUserIdentity user)
                {
                    return user.getUserId();
                }   
            }); 
    }

}

// ##########################################################################
