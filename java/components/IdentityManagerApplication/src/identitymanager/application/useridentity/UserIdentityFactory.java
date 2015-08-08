// ##########################################################################
// # File Name:	UserIdentityFactory.java
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

package identitymanager.application.useridentity;

import identitymanager.service.shared.UserIdentityDescriptor;
import identitymanager.service.useridentity.FinishCreatingUserIdentityReply;
import identitymanager.service.useridentity.FinishCreatingUserIdentityRequest;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityReply;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.PersonNameData;
import identitymanager.service.useridentity.StartCreatingUserIdentityReply;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import identitymanager.service.useridentity.StartUpdatingUserIdentityReply;
import identitymanager.service.useridentity.StartUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.UpdateUserIdentityReply;
import identitymanager.service.useridentity.UpdateUserIdentityRequest;
import identitymanager.service.useridentity.UserIdentityData;
import identitymanager.service.useridentity.UserIdentityReply;
import identitymanager.service.useridentity.UserIdentityRequest;
import identitymanager.domain.useridentity.IUserIdentity;
import identitymanager.domain.useridentity.IUserIdentityEvent;
import identitymanager.domain.useridentity.UserIdentity;
import identitymanager.domain.useridentity.UserIdentityEvent;
import identitymanager.domain.useridentity.UserIdentityEventKind;
import identitymanager.domain.useridentity.UserIdentityStateKind;
import identitymanager.common.utility.IIdentifierGenerator;
import identitymanager.common.utility.ITimestampGenerator;
import identitymanager.common.value.EmailAddress;
import identitymanager.common.value.PersonName;
import strata1.common.datetime.DateTime;
import javax.inject.Inject;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentityFactory
    implements IUserIdentityFactory
{
    private final IIdentifierGenerator itsIdGenerator;
    private final ITimestampGenerator  itsTimeGenerator;
    
    /************************************************************************
     * Creates a new UserIdentityFactory. 
     *
     */
    @Inject
    public 
    UserIdentityFactory(
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
    public StartCreatingUserIdentityReply 
    createUserIdentityReply(StartCreatingUserIdentityRequest request)
    {
        StartCreatingUserIdentityReply reply = 
            new StartCreatingUserIdentityReply();
        
        initializeUserIdentityReply( reply,request );
        return reply;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public FinishCreatingUserIdentityReply 
    createUserIdentityReply(FinishCreatingUserIdentityRequest request)
    {
        FinishCreatingUserIdentityReply reply = 
            new FinishCreatingUserIdentityReply();
        
        initializeUserIdentityReply( reply,request );
        return reply;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public StartUpdatingUserIdentityReply 
    createUserIdentityReply(StartUpdatingUserIdentityRequest request)
    {
        StartUpdatingUserIdentityReply reply = 
            new StartUpdatingUserIdentityReply();
        
        initializeUserIdentityReply( reply,request );
        return reply;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public FinishUpdatingUserIdentityReply 
    createUserIdentityReply(FinishUpdatingUserIdentityRequest request)
    {
        FinishUpdatingUserIdentityReply reply = 
            new FinishUpdatingUserIdentityReply();
        
        initializeUserIdentityReply( reply,request );
        return reply;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public UpdateUserIdentityReply 
    createUserIdentityReply(UpdateUserIdentityRequest request)
    {
        UpdateUserIdentityReply reply = 
            new UpdateUserIdentityReply();
        
        initializeUserIdentityReply( reply,request );
        return reply;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IUserIdentityEvent 
    createUserIdentityEvent(StartCreatingUserIdentityRequest request)
    {
        IUserIdentityEvent event = new UserIdentityEvent();
        UserIdentityData   user  = request.getUser();
        PersonNameData     name  = user.getPersonName();
        
        event.setTimestamp( new DateTime() );
        event.setEventKind( UserIdentityEventKind.CREATE_USERIDENTITY );
        event.setStateKind( UserIdentityStateKind.PENDING );
        event.setUserName( user.getUserName() );
        event.setPassword( user.getUserPassword() );
        event.setEmailAddress( new EmailAddress(user.getEmailAddress() ) );
        event.setEmailVerification( 
            itsIdGenerator
                .getNextIntegerId( 10000 )
                .toString() );
        event.setName( 
            new PersonName(
                name.getFirstName(),
                name.getMiddleName(),
                name.getLastName() ) );
        
        return event;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IUserIdentity 
    createUserIdentity(IUserIdentityEvent event)
    {
        IUserIdentity identity = new UserIdentity();
        
        identity.setUserId( event.getUserId() );
        identity.setUserName( event.getUserName() );
        identity.setPassword( event.getPassword() );
        identity.setEmailAddress( event.getEmailAddress() );
        identity.setName( event.getName() );
        identity.setPostalAddress( event.getPostalAddress() );
        identity.setPhoneNumber( event.getPhoneNumber() );
        identity.setSecurityQuestion( event.getSecurityQuestion() );
        
        return identity;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public UserIdentityDescriptor 
    createUserIdentityDescriptor(IUserIdentity identity)
    {
        UserIdentityDescriptor descriptor = new UserIdentityDescriptor();
        
        descriptor.setUserId( identity.getUserId() );
        descriptor.setUserName( identity.getUserName() );
        return descriptor;
    }

    /************************************************************************
     *  
     *
     * @param reply
     * @param request
     */
    private void 
    initializeUserIdentityReply(
        UserIdentityReply   reply,
        UserIdentityRequest request)
    {
        reply.setReplyId( itsIdGenerator.getNextLongId().toString() );
        reply.setOriginatingRequestId( request.getRequestId() );
        reply.setSessionId( request.getSessionId() );
        reply.setTimestamp( itsTimeGenerator.getCurrent() );
        reply.setDuplicate( false );
    }

}

// ##########################################################################
