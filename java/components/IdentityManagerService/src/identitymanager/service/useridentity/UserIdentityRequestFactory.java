// ##########################################################################
// # File Name:	UserIdentityRequestFactory.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerService Framework.
// #
// #   			The IdentityManagerService Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerService Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerService
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.service.useridentity;

import identitymanager.common.utility.IIdentifierGenerator;
import identitymanager.common.utility.ITimestampGenerator;
import javax.inject.Inject;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentityRequestFactory
    implements IUserIdentityRequestFactory
{
    private final IIdentifierGenerator itsIdGenerator;
    private final ITimestampGenerator  itsTimeGenerator;
    
    /************************************************************************
     * Creates a new UserIdentityRequestFactory. 
     *
     * @param idGenerator
     * @param timeGenerator
     */
    @Inject
    public 
    UserIdentityRequestFactory(
        IIdentifierGenerator idGenerator,
        ITimestampGenerator  timeGenerator)
    {
        itsIdGenerator   = idGenerator;
        itsTimeGenerator = timeGenerator;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public StartCreatingUserIdentityRequest 
    createStartCreatingUserIdentityRequest(UserIdentityData newUser)
    {
        StartCreatingUserIdentityRequest request = 
            new StartCreatingUserIdentityRequest();
        
        if ( isExcluded( newUser.getEmailAddress() ) )
            throw 
                new IllegalArgumentException( 
                    "email address must be included" );
        
        setUserIdentityRequestFields( request );
        request.setUser( newUser );
        
        return request;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public FinishCreatingUserIdentityRequest 
    createFinishCreatingUserIdentityRequest(
        Long   pendingEventId, 
        String verificationCode)
    {
        FinishCreatingUserIdentityRequest request = 
            new FinishCreatingUserIdentityRequest();
        
        setUserIdentityRequestFields( request );
        request.setPendingEventId( pendingEventId );
        request.setVerificationCode( verificationCode );
        
        return request;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public StartUpdatingUserIdentityRequest 
    createStartUpdatingUserIdentityRequest(UserIdentityData updatedUser)
    {
        StartUpdatingUserIdentityRequest request = 
            new StartUpdatingUserIdentityRequest();
        
        if ( isExcluded( updatedUser.getEmailAddress() ) )
            throw 
                new IllegalArgumentException( 
                    "email address must be included" );
        
        setUserIdentityRequestFields( request );
        request.setUser( updatedUser );
        
        return request;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public FinishUpdatingUserIdentityRequest 
    createFinishUpdatingUserIdentityRequest(
        Long   pendingEventId,
        String verificationCode)
    {
        FinishUpdatingUserIdentityRequest request = 
            new FinishUpdatingUserIdentityRequest();
        
        setUserIdentityRequestFields( request );
        request.setPendingEventId( pendingEventId );
        request.setVerificationCode( verificationCode );
        
        return request;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public UpdateUserIdentityRequest 
    createUpdateUserIdentityRequest(UserIdentityData updatedUser)
    {
        UpdateUserIdentityRequest request = 
            new UpdateUserIdentityRequest();
        
        if ( isIncluded( updatedUser.getEmailAddress() ) )
            throw 
                new IllegalArgumentException( 
                    "email address must be excluded" );
        
        setUserIdentityRequestFields( request );
        
        return request;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public StartListeningRequest 
    createStartListeningRequest()
    {
        StartListeningRequest request = 
            new StartListeningRequest();
        
        setUserIdentityRequestFields( request );
        
        return request;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public StopListeningRequest 
    createStopListeningRequest()
    {
        StopListeningRequest request = 
            new StopListeningRequest();
        
        setUserIdentityRequestFields( request );
        
        return request;
    }

    /************************************************************************
     *  
     *
     * @param request
     */
    private void 
    setUserIdentityRequestFields(UserIdentityRequest request)
    {
        request.setRequestId( itsIdGenerator.getNextStringId() );
        request.setTimestamp( itsTimeGenerator.getCurrent() );
        request.setDuplicate( false );
    }

    /************************************************************************
     *  
     *
     * @param field
     * @return
     */
    private boolean 
    isExcluded(String field)
    {
        return 
            field == null ||
            field.isEmpty();
    }

    /************************************************************************
     *  
     *
     * @param field
     * @return
     */
    private boolean 
    isIncluded(String field)
    {
        return !isExcluded( field );
    }

}

// ##########################################################################
