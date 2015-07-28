// ##########################################################################
// # File Name:	UserIdentityRepository.java
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

import javax.inject.Inject;
import strata1.persistence.repository.IFinder;
import strata1.persistence.repository.IRepositoryContext;
import strata1.persistence.repository.IUnitOfWork;
import strata1.persistence.repository.InsertFailedException;
import strata1.persistence.repository.InvalidInputException;
import strata1.persistence.repository.NotUniqueException;
import strata1.persistence.repository.RemoveFailedException;
import strata1.persistence.repository.UpdateFailedException;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentityRepository
    implements IUserIdentityRepository
{
    private final IUserIdentityRepositoryProvider itsProvider;
    
    /************************************************************************
     * Creates a new UserIdentityRepository. 
     *
     */
    @Inject
    public 
    UserIdentityRepository(IUserIdentityRepositoryProvider provider)
    {
        itsProvider = provider;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IRepositoryContext 
    getContext()
    {
        return itsProvider.getContext();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IUnitOfWork 
    getUnitOfWork()
    {
        return getContext().getUnitOfWork();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IUserIdentity 
    insertUserIdentity(IUserIdentity newUser) 
        throws InsertFailedException
    {
        return itsProvider.insertNew( newUser );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IUserIdentity 
    updateUserIdentity(IUserIdentity existingUser) 
        throws UpdateFailedException
    {
        return itsProvider.updateExisting( existingUser );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    removeUserIdentity(IUserIdentity existingUser) 
        throws RemoveFailedException
    {
        itsProvider.removeExisting( existingUser );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IUserIdentity 
    getUserIdentity(String userId)
    {
        return itsProvider.getExisting( userId );
    }

    /************************************************************************
     * {@inheritDoc} 
     * @throws  
     */
    @Override
    public IUserIdentity 
    getUserIdentityWithUserName(String userName) 
        throws InvalidInputException,NotUniqueException
    {
        IFinder<IUserIdentity> finder = 
            getFinder("getUserIdentityWithUserName");
        
        finder.setInput( "userName",userName );
        return finder.getUnique();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    hasUserIdentity(String userId)
    {
        return itsProvider.hasExisting( userId );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    hasUserIdentityWithUserName(String userName)
        throws InvalidInputException,NotUniqueException
    {
        IFinder<IUserIdentity> finder = 
            getFinder("hasUserIdentityWithUserName");
        
        finder.setInput( "userName",userName );
        return finder.hasUnique();
    }

    /************************************************************************
     *  
     *
     * @param finderName
     * @return
     */
    private IFinder<IUserIdentity> 
    getFinder(String finderName)
    {
        return 
            itsProvider.getFinder( 
                "identitymanager.domain.useridentity.IUserIdentity." + 
                finderName );
    }

}

// ##########################################################################
