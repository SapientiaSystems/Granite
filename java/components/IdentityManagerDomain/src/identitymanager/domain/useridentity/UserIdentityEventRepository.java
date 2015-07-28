// ##########################################################################
// # File Name:	UserIdentityEventRepository.java
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
class UserIdentityEventRepository
    implements IUserIdentityEventRepository
{
    private final IUserIdentityEventRepositoryProvider itsProvider;
    
    /************************************************************************
     * Creates a new UserIdentityRepository. 
     *
     */
    @Inject
    public 
    UserIdentityEventRepository(
        IUserIdentityEventRepositoryProvider provider)
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
    public IUserIdentityEvent 
    insertUserIdentityEvent(IUserIdentityEvent newEvent) 
        throws InsertFailedException
    {
        return itsProvider.insertNew( newEvent );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IUserIdentityEvent 
    updateUserIdentityEvent(IUserIdentityEvent existingEvent) 
        throws UpdateFailedException
    {
        return itsProvider.updateExisting( existingEvent );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    removeUserIdentityEvent(IUserIdentityEvent existingEvent) 
        throws RemoveFailedException
    {
        itsProvider.removeExisting( existingEvent );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IUserIdentityEvent 
    getUserIdentityEvent(Long eventId)
    {
        return itsProvider.getExisting( eventId );
    }

    /************************************************************************
     * {@inheritDoc} 
     * @throws  
     */
    @Override
    public IUserIdentityEvent 
    getUserIdentityEventWithUserName(String userName) 
        throws InvalidInputException,NotUniqueException
    {
        IFinder<IUserIdentityEvent> finder = 
            getFinder("getUserIdentityEventWithUserName");
        
        finder.setInput( "userName",userName );
        return finder.getUnique();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    hasUserIdentityEvent(Long eventId)
    {
        return itsProvider.hasExisting( eventId );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    hasUserIdentityEventWithUserName(String userName)
        throws InvalidInputException,NotUniqueException
    {
        IFinder<IUserIdentityEvent> finder = 
            getFinder("hasUserIdentityEventWithUserName");
        
        finder.setInput( "userName",userName );
        return finder.hasUnique();
    }

    /************************************************************************
     *  
     *
     * @param finderName
     * @return
     */
    private IFinder<IUserIdentityEvent> 
    getFinder(String finderName)
    {
        return 
            itsProvider.getFinder( 
                "identitymanager.domain.useridentity.IUserIdentityEvent." + 
                finderName );
    }

}

// ##########################################################################
