// ##########################################################################
// # File Name:	HibernateUserIdentityRepositoryProvider.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerServerPlatform Framework.
// #
// #   			The IdentityManagerServerPlatform Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerServerPlatform Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerServerPlatform
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.serverplatform.hibernatepersistence;

import identitymanager.domain.useridentity.IUserIdentityEvent;
import identitymanager.domain.useridentity.IUserIdentityEventRepositoryProvider;
import strata1.persistence.hibernaterepository.HibernateRepositoryContext;
import strata1.persistence.hibernaterepository.HibernateRepositoryProvider;
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
class HibernateUserIdentityEventRepositoryProvider
    extends    HibernateRepositoryProvider<Long,IUserIdentityEvent>
    implements IUserIdentityEventRepositoryProvider
{

    /************************************************************************
     * Creates a new HibernateUserIdentityRepositoryProvider. 
     *
     * @param context
     */
    @Inject
    public
    HibernateUserIdentityEventRepositoryProvider(IRepositoryContext context)
    {
        super( 
            IUserIdentityEvent.class,
            (HibernateRepositoryContext)context );
    }    
}

// ##########################################################################
